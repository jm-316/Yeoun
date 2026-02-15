let safeStockList = [];


const safeStockModal = new bootstrap.Modal(document.getElementById("safeStock-modal"));
const itemModal = new bootstrap.Modal(document.getElementById("item-modal"));

const safeStockGrid = new tui.Grid({
	el: document.getElementById("safeStockGrid"),
	bodyHeight: 500,
	rowHeaders: ["checkbox"],
	pageOptions: {
	    useClient: true,  // 클라이언트 사이드 페이징
	    perPage: 20       // 페이지당 20개 행
	},
	columnOptions: {
		resizable: true
	},
	columns: [
			{
				header: "SAFEID",
				name: "safeId",
				hidden: true
			},
			{
				header: "ITEMID",
				name: "itemId",
				hidden: true
			},
			{
				header: "품목코드",
				name: "itemCode",
				sortable: true,
				sortingType: 'asc',
			},
			{
				header: "품목명",
				name: "itemName",
				sortable: true,
				sortingType: 'asc',
			},
			{
				header: "품목타입",
				name: "itemType",
				formatter: ({value}) => {
					if (value === "PRD") {
						return "완제품";
					} else if (value === "MAT") {
						return "원재료";
					} else {
						return "";
					}
				}
			},
			{
				header: "단위",
				name: "itemUnit",
			},
			{
				header: "일 생산량",
				name: "dailyCapa",
				align: 'right',
				formatter: ({ value }) =>
					value == null ? '' : `${Number(value).toLocaleString()}`,
				editor: "text"
			},
			{
				header: "일일 소요 기준량",
				name: "dailyReqQty",
				align: 'right',
				formatter: ({ value }) =>
					value == null ? '' : `${Number(value).toLocaleString()}`,
				editor: "text"
			},
			{
				header: "목표 보관 일수",
				name: "targetDays",
				align: 'right',
				formatter: ({ value }) =>
					value == null ? '' : `${Number(value).toLocaleString()}`,
				editor: "text"
			},
			{
				header: "총 안전재고",
				name: "totalSafeQty",
				align: 'right',
				formatter: ({ value }) =>
					value == null ? '' : `${Number(value).toLocaleString()}`,
			},
			{
				header: "비고",
				name: "remark",
				editor: "text"
				
			}
		]
});

// 변경하기 전 값
const beforeEditsafeValues = {};

safeStockGrid.on("editingStart", ev => {
    const { rowKey, columnName } = ev;

    beforeEditsafeValues[rowKey] ??= {};
    beforeEditsafeValues[rowKey][columnName] =
        safeStockGrid.getValue(rowKey, columnName);
});

const validationSafeRules = {
	dailyCapa: {
		required: true,
		pattern: /^\d+(\.\d{1,2})?$/,
		errorMessage: "숫자만 입력 가능하며 소수점은 둘째 자리까지 허용됩니다."
	},
	dailyReqQty: {
		required: true,
		pattern: /^\d+(\.\d{1,2})?$/,
		errorMessage: "숫자만 입력 가능하며 소수점은 둘째 자리까지 허용됩니다."
	},
	targetDays: {
		required: true,
		pattern: /^[0-9]+$/,
		errorMessage: "숫자만 입력 가능합니다."
	},
}

// 통합 유효성 검사
function validateSafeField(rowKey, columnName, value) {
	const rule = validationSafeRules[columnName];
	
	// 검증 규칙이 없으면 통과
	if (!rule) {
		return true;
	}
	
	// 빈 값이고 필수가 아니면 통과
	if (!value || value === "") {
		return true;
	}
	
	const stringValue = String(value).trim();
	
	// 패턴 체크
	if (rule.pattern && !rule.pattern.test(stringValue)) {
		alert(rule.errorMessage);
		restoreValue(rowKey, columnName);
		return false;
	} 
	
	return true;
}

// 이전 값으로 복원
function restoreValue(rowKey, columnName) {
	const beforeValue = beforeEditsafeValues?.[rowKey]?.[columnName] || "";
	setTimeout(() => {
		safeStockGrid.setValue(rowKey, columnName, beforeValue);
	}, 0);
}

safeStockGrid.on("editingFinish", ev => {
    const { rowKey, columnName, value } = ev;
	
	// 통합 검증
	if (!validateSafeField(rowKey, columnName, value)) return;
	
	const calcColumns = ["dailyCapa", "dailyReqQty", "targetDays"];
	
	if (!calcColumns.includes(columnName)) return;
	
	// 현재 row 값 가져오기
	const dailyReqQty = Number(safeStockGrid.getValue(rowKey, "dailyReqQty"));
	const targetDays  = Number(safeStockGrid.getValue(rowKey, "targetDays"));
	
	if (!dailyReqQty || !targetDays) {
		safeStockGrid.setValue(rowKey, "totalSafeQty", null);
	}
	
	const totalSafeQty = dailyReqQty * targetDays;
	
	safeStockGrid.setValue(rowKey, "totalSafeQty", totalSafeQty)
		
//  	const beforeValue = beforeEditsafeValues?.[rowKey]?.[columnName];
	
});

// 클릭 동작
safeStockGrid.on('click', async (ev) => {
	const { columnName, rowKey } = ev;
	
    if (rowKey == null) return;
	
	const rowData = safeStockGrid.getRow(rowKey);
	
	if ((columnName === "itemName" || columnName === "itemCode") && rowData.safeId === null) {
		try {
			// 원재료와 완제품 조회할 수 있는 모달 열기
			itemModal.show();
			
			window.selectedBomRowKey = rowKey;
			
			const modalElement = document.getElementById('item-modal');
				modalElement.addEventListener('shown.bs.modal', async function loadData() {
			    	await loaditems();
			      
			      	// 그리드 강제 리프레시
			      	itemGrid.refreshLayout();
			      
			      	// 이벤트 리스너 제거 (한 번만 실행되도록)
			      	modalElement.removeEventListener('shown.bs.modal', loadData);
			  }, { once: true }); // once 옵션으로 자동 제거
		} catch (error) {
			
		}
	}
	
    if (columnName === "useYn") {
        safeStockGrid.startEditing(rowKey, columnName);
		return;
    }
});

// 안전재고 정보 불러오기
async function loadSafeStock() {
	const SAFE_STOCK_URL = "/safeStock/list";
			
	try {
		const res = await fetch(apiUrl(SAFE_STOCK_URL), {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		const data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			safeStockGrid.resetData([]);
		}

		safeStockGrid.resetData(data);
		
		safeStockList = data;
		
	} catch (error) {
		console.error(error);
	}
}

document.getElementById("safeStockBtn").addEventListener("click", async () => {
	
	safeStockModal.show();
	
	const modalElement = document.getElementById('safeStock-modal');
		modalElement.addEventListener('shown.bs.modal', async function loadData() {
	    	await loadSafeStock();
	      
	      	// 그리드 강제 리프레시
	      	safeStockGrid.refreshLayout();
	      
	      	// 이벤트 리스너 제거 (한 번만 실행되도록)
	      	modalElement.removeEventListener('shown.bs.modal', loadData);
	}, { once: true }); // once 옵션으로 자동 제거
});

// 추가 번튼 이벤트
document.getElementById("addSafetyStockRowBtn").addEventListener("click", () => {
	safeStockGrid.prependRow();
});

const MAT_TYPE_VALUES = matTypeList.map(v => v.value);

const normalizeItemType = (itemType) => {
	if (itemType === "RRD") return "PRD";
	
	if (MAT_TYPE_VALUES.includes(itemType)) return "MAT";
	
	return itemType;
}

document.getElementById("saveSafetyStockRowBtn").addEventListener("click", async () => {
	// 편집 완료
	safeStockGrid.finishEditing();
	
	const modifiedData = safeStockGrid.getModifiedRows() || {};
	const updateRows = modifiedData.updatedRows || [];
	let createdRows = modifiedData.createdRows || []; 
	
	const isEmptyRos = (row) => {
		return !row.itemId && !row.itemCode && !row.dailyReqQty && !row.targetDays && !row.dailyCapa && !row.totalSafeQty;
	}
	
	if (createdRows.itemId === undefined && updateRows.length === 0) {
		alert("변경된 내역이 없습니다.");
		return;
	}
	
	createdRows = createdRows.filter(row => !isEmptyRos(row))
							 .map(row => ({
								...row,
								itemType: normalizeItemType(row.itemType)
							 }));
	
	const saveData = {
		created: createdRows,
		updated: updateRows
	}
	
	await savaSafeStock(saveData);
});

document.getElementById("deleteSafetyStockRowBtn").addEventListener("click", async () => {
	const checkedRows = safeStockGrid.getCheckedRowKeys();
	
	if (checkedRows.length === 0) {
		aert("삭제할 품목을 선택해주세요.");
		return;
	}
	
	// 삭제할 때 사용할 safeId 배열
	const safeIds = checkedRows.map(rowKey => {
		const rowData = safeStockGrid.getRow(rowKey);
		return rowData.safeId;
	});
	
	const validSafeIds = safeIds.filter(item => item !== null && item !== undefined)
								.map(String);
								
	if (!confirm(`${checkedRows.length}개의 항목을 삭제하시겠습니까?`)) {
		return;
	}
	
	if (validSafeIds.length === 0) {
		safeStockGrid.removeCheckedRows();
		alert("삭제되었습니다.");
		return;
	}
	
	await deleteSafeStock(validSafeIds);
});

async function savaSafeStock(data) {
	const SAVE_SAFE_STOCK_URL = "/safeStock/list/modify";
	
	try {
		const res = await fetch(apiUrl(SAVE_SAFE_STOCK_URL), {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data)
		});
		
		if (!res.ok) {
			throw new Error("다시 시도해주세요");
		}
		
		itemModal.hide();
		
		document.getElementById("materialKeyword").value = "";
		alert("저장되었습니다.");
		
		await loadSafeStock();
		
	} catch (error) {
		console.error(error);
		alert("저장 실패했습니다.");
	}
}


async function deleteSafeStock(validSafeIds) {
	console.log(validSafeIds)
	const SAFE_STOCK_DELETE_URL = "/safeStock/data/delete";
	try {
		const res = await fetch(apiUrl(SAFE_STOCK_DELETE_URL), {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(validSafeIds)
		});
		
		if (!res.ok) {
			throw new Error("삭제 중 문제가 발생했습니다.");
		}
		
		safeStockGrid.removeCheckedRows();
		
		alert("삭제되었습니다.");
		
		await loadSafeStock();
	} catch (error) {
		console.error(error);
		alert("삭제에 실패했습니다.")
	}
	
	
}