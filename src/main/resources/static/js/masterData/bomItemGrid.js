const bomItemGrid = new tui.Grid({
	el: document.getElementById("bomItemGrid"),
	bodyHeight: 500,
	rowHeaders: ['checkbox'],
	pageOptions: {
	    useClient: true,  // 클라이언트 사이드 페이징
	    perPage: 20       // 페이지당 20개 행
	},
	columnOptions: {
		resizable: true
	},
	columns: [
			{
				header: "BOMID",
				name: "bomId",
				hidden: true
				
			},
			{
				header: "BOMITEMID",
				name: "bomItemId",
				hidden: true
				
			},
			{
				header: "MATID",
				name: "matId",
				hidden: true
				
			},
			{
				header: "원재료명",
				name: "matName",
			},
			{
				header: "사용량",
				name: "bomQty",
				editor: "text"
			},
			{
				header: "단위",
				name: "bomUnit",
			},
		]
});

// bom 정보 불러오기
async function loadBomItem(bomId) {
	const BOM_ITEM_LIST = `/bomMst/bomItems/${bomId}`;
			
	try {
		bomItemGrid.resetData([]); // 기존 데이터 초기화
		
		const res = await fetch(BOM_ITEM_LIST, {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		const data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			bomItemGrid.resetData([]);
		}

		bomItemGrid.resetData(data);
		
	} catch (error) {
		console.error(error);
	}
}

bomItemGrid.on('click', async (ev) => {
	const { columnName, rowKey } = ev;
	
	const rowData = bomItemGrid.getRow(ev.rowKey);
	
	if (columnName === "matName" && rowData.bomItemId === null) {
		try {
			bomItemGrid.disable();
			
			// 원재료 모달 열기
			matItemsModal.show();
			
			// 현재 선택된 row 정보 저장
			window.selectedBomRowKey = rowKey;
			
			const modalElement = document.getElementById('matItems-modal');
	        	modalElement.addEventListener('shown.bs.modal', async function loadData() {
	            	await loadMaterial("Y");
	              
	              	// 그리드 강제 리프레시
	              	materialItemGrid.refreshLayout();
	              
	              	// 이벤트 리스너 제거 (한 번만 실행되도록)
	              	modalElement.removeEventListener('shown.bs.modal', loadData);
	          }, { once: true }); // once 옵션으로 자동 제거
		} catch (error) {
			console.error(error);
		}
	}
});

// 변경하기 전 값
const beforeEditItemValues = {};
// BOM_ITEM 수정 확인 변수
let isBomItemEdited = false;

bomItemGrid.on("editingStart", ev => {
    const { rowKey, columnName } = ev;
	
	isBomItemEdited = true;

    beforeEditItemValues[rowKey] ??= {};
    beforeEditItemValues[rowKey][columnName] =
        bomItemGrid.getValue(rowKey, columnName);
});

const validationItemRules = {
	bomQty: {
		required: true,
		pattern: /^\d+(\.\d{1,2})?$/,
		errorMessage: "숫자만 입력 가능하며 소수점은 둘째 자리까지 허용됩니다."
	}
}

// 통합 유효성 검사
function validateItemField(rowKey, columnName, value) {
	const rule = validationItemRules[columnName];
	
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
	const beforeValue = beforeEditItemValues?.[rowKey]?.[columnName] || "";
	setTimeout(() => {
		bomItemGrid.setValue(rowKey, columnName, beforeValue);
	}, 0);
}

bomItemGrid.on("editingFinish", ev => {
    const { rowKey, columnName, value } = ev;
	
	const before = beforeEditItemValues[rowKey]?.[columnName];
	
	// 통합 검증
	if (!validateItemField(rowKey, columnName, value)) return;
	
	if (before !== value) {
		isBomItemEdited = true;
	}
});

async function loadMaterial(useYn) {
	const MATERIAL_LIST = `/masterData1/data/materialList?useYn=${useYn}`;
			
	try {
		const res = await fetch(MATERIAL_LIST, {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		const data = await res.json();
		
		materialItemGrid.resetData(data);
		materialItemData = data;
		
	} catch (error) {
		console.error(error);
	} finally {
		bomItemGrid.enable();
	}
}

// bom Item 추가 버튼 이벤트
document.getElementById("bomItemRegistBtn").addEventListener("click", () => {
	// bom을 선택했을 때 bom item 추가할 수 있음
	if (isSelect) {
		bomItemGrid.prependRow({
		    bomItemId: null,        // 신규 항목
		    bomId: selectedBomId,   // 선택된 BOM의 ID
		    matId: null,
		    matName: null,
		    bomQty: 0,
		    bomUnit: null,
		});
	}
	
	targetGrid = "bomItem";
});


// bom Item 저장
document.getElementById("bomItemSaveBtn").addEventListener("click", async () => {
	// 편집 완료
	bomItemGrid.finishEditing();
	bomGrid.finishEditing();
	
	if (!isBomEdited && !isBomItemEdited) {
		alert("수정된 내용이 없습니다.");
		return;
	}
	
	let successMessage = [];
	
	try {
		if (isBomEdited) {
			const bomData = collectBomData();
			await modifyBom(bomData);
			successMessage.push("BOM 정보");
		}
		
		if (isBomItemEdited) {
			const bomItemData = collectBomItemData();
			await saveBomItem(bomItemData);
			successMessage.push("BOM 원재료");
		}
		
		if (successMessage.length > 0) {
			alert(`${successMessage.join(", ")}가 성공적으로 저장되었습니다.`);
		}
		
		if (isBomEdited) {
			await loadBom();
		}
		
		if (isBomItemEdited) {
			await loadBomItem(selectedBomId);
		}
		
	} catch (error) {
		console.error(error);
		alert(error.message || "저장에 실패했습니다.");
	}
});

function collectBomItemData() {
	const modifiedData = bomItemGrid.getModifiedRows() || {};
	const updateRows = modifiedData.updatedRows || [];
	let createdRows = modifiedData.createdRows || [];
	
	const isEmptyRow = (row) => {
		return !row.matId && !row.matCode && !row.matName && !row.bomQty;
	}
	
	createdRows = createdRows.filter(row => !isEmptyRow(row));
	
	return {
		created: createdRows,
		updated: updateRows
	}
}

async function saveBomItem(data) {
	const BOM_ITEM_ADD_URL = "/bomMst/data/bomItem/add";
	
	try {
		const res = await fetch(apiUrl(BOM_ITEM_ADD_URL), {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data)
		});
		
		if (!res.ok) {
		    let message = `등록 실패 (${res.status})`;

		    try {
		        const errorText = await res.text();
				if (errorText) {
				    message = errorText;
				}
		    } catch (_) {}

		    throw new Error(message);
		}
		
	} catch (error) {
		console.error(error);
		alert(error.message || "저장에 실패했습니다.");
	}
}

// row 삭제 버튼
document.getElementById("bomItemDeleteBtn").addEventListener("click", async () => {
	const checkedRows = bomItemGrid.getCheckedRowKeys();
	
	if (checkedRows.length === 0) {
		alert("삭제할 원재료를 선택해주세요.");
		return;
	}
	
	// 삭제할 때 사용할 bomItemId 배열
	const bomItemIds = checkedRows.map(rowKey => {
		const rowData = bomItemGrid.getRow(rowKey);
		return rowData.bomItemId;
	});
	
	// null 과 undefined 필터링
	const validBomItemIds = bomItemIds.filter(item => item !== null && item !== undefined)
									  .map(String);
									  
	if (!confirm(`${checkedRows.length}개의 항목을 삭제하시겠습니까?`)) {
		return;
	}
	
	if (validBomItemIds.length === 0) {
		bomItemGrid.removeCheckedRows();
		alert('삭제되었습니다.');
		return;
	}
	
	await deleteBomItem(validBomItemIds);
});

async function deleteBomItem(bomItemIds) {
	console.log(bomItemIds)
	try {
		const BOM_ITEM_DELETE_URL = "/bomMst/data/bomItem/delete";
		
		const res = await fetch(apiUrl(BOM_ITEM_DELETE_URL), {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(bomItemIds)
		});
		
		if (!res.ok) {
		    let message = `등록 실패 (${res.status})`;

		    try {
		        const errorText = await res.text();
				if (errorText) {
				    message = errorText;
				}
		    } catch (_) {}

		    throw new Error(message);
		}
		
		bomItemGrid.removeCheckedRows();
		
		alert("삭제되었습니다.");
		
	} catch (error) {
		console.error(error);
		alert(error.message || "저장에 실패했습니다.");
	}
}