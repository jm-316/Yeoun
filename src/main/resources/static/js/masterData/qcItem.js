let qcItemList = [];
let targetTypeList = [];
let isEdit = false;

// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async () => {
	await loadTargetType();
	await qcItemGridAllSearch(); //품질항목기준
});

// 품질항목기준
const qcItemGrid = new tui.Grid({
	el: document.getElementById('qcItemGrid'), 
	rowHeaders: ['rowNum'],
	pageOptions: {
	    useClient: true,  // 클라이언트 사이드 페이징
	    perPage: 20       // 페이지당 20개 행
	},
	columnOptions: {
		resizable: true
	},
	columns: [
		{
			header: "QC항목ID",
			name: "qcItemId"
		},
		{
			header: "항목명",
			name: "itemName"
		},
		{
			header: "대상구분",
			name: "targetType",
			filter: "select",
			formatter: ({value}) => {
				const type = targetTypeList.find(item => item.value === value);
				return type ? type.text : value;
			}
		},
		{
			header: "단위",
			name: "unit"
		},
		{
			header: "기준 텍스트",
			name: "stdText"
		},
		{
			header: "MIN",
			name: "minValue"
		},
		{
			header: "MAX",
			name: "maxValue"
		},
		{
			header: "사용여부",
			name: "useYn",
			filter: "select",
			renderer:{ type: StatusModifiedRenderer},
			formatter: ({value}) => {
				const statusMap = {
					"Y": "활성",
					"N": "비활성"
				};
				return statusMap[value] || value;
			}
		},
		{
			header: "상세보기",
			name: "view_details",
			formatter: (rowInfo) => {
				return `<button type='button' class='btn btn-primary btn-sm' data-row-key='${rowInfo.row.rowKey}'>상세</button>`;
			}
		},
	]
});

// qcItem 조회
async function qcItemGridAllSearch() {
	try {
		const res = await fetch(apiUrl(`/masterData/qcItem/list`));
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패");
		}
		
		const data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			qcItemGrid.resetData([]);
		} 
		
		qcItemGrid.resetData(data);
		qcItemList = data;
	} catch (error) {
		console.log(error);
	}
}

qcItemGrid.on("click", async (ev) => {
	const { targetType, columnName, rowKey } = ev;
	
	if (targetType === "cell" && columnName === "view_details") {
		
		const rowData = qcItemGrid.getRow(rowKey);
		
		const qcItemId = rowData.qcItemId;
		
		const qcItem = await loadQcItemDetail(qcItemId);
		
		// 예: 모달 열기, 상세 정보 표시 등		
		$('#qcItem-modal').modal('show');
		
		isEdit = true;
		fillModal(qcItem);

		await qcItemGridAllSearch();
	}
});

// qcItem 상세 조회
async function loadQcItemDetail(qcItemId) {
	try {
		const res = await fetch(apiUrl(`/masterData/qcItem/${qcItemId}`));
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패");
		}
		
		const data = await res.json();
		
		return data;
	} catch (error) {
		console.error(error);
	}
}

// 상세 모달 
function fillModal(data) {
	document.getElementById('qcmodalTilte').innerText= 'QC 항목 상세';
	document.getElementById('modalQcItemId').value = data.qcItemId;//QC 항목 ID
	document.getElementById('itemName').value = data.itemName;//항목명
	document.getElementById('targetType').value = data.targetType;//대상구분
	document.getElementById('unit').value = data.unit;//단위
	document.getElementById('stdText').value = data.stdText;//기준텍스트
	document.getElementById('minValue').value = data.minValue;//최소값
	document.getElementById('maxValue').value = data.maxValue;//최대값
	document.getElementById('sortOrder').value = data.sortOrder;//정렬순서
	document.getElementById('useYn').value = data.useYn;//사용여부

	document.getElementById('modalQcItemId').readOnly = true;
}


// QC 항목 등록 버튼 이벤트
document.getElementById("qcItemRegistBtn").addEventListener("click", () => {
	document.getElementById('qcmodalTilte').innerText= 'QC 항목 등록';
	document.getElementById('modalQcItemId').readOnly = false;
	qcModalreset();
});

function qcModalreset() {
	document.getElementById('modalQcItemId').value = '';//QC 항목 ID
	document.getElementById('itemName').value = '';//항목명
	document.getElementById('targetType').value = '';//대상구분
	document.getElementById('unit').value = '';//단위
	document.getElementById('stdText').value = '';//기준텍스트
	document.getElementById('minValue').value = '';//최소값
	document.getElementById('maxValue').value = '';//최대값
	document.getElementById('sortOrder').value = '';//정렬순서
	document.getElementById('useYn').value = '';//사용여부
	
	document.getElementById('modalQcItemId').readOnly = false;
	isEdit = false;
}

// 수정 또는 상세 모달의 저장 버튼 이벤트
document.getElementById("saveQcItem").addEventListener("click", async () => {
	const data = await getQcItemData();
	
	if (!data) return;
	
	if (isEdit) {
		const qcItemId = document.getElementById('modalQcItemId').value;
		await updateQcItem(qcItemId, data);
	} else {
		await insertQcItem(data);
	}
	
	await qcItemGridAllSearch();
	$('#qcItem-modal').modal('hide');
	$('.modal-backdrop').remove();
	$('body').removeClass('modal-open');
	$('body').css('padding-right', '');
});

// qcItem 업데이트 함수 
async function updateQcItem(qcItemId, data) {
	try {
		const res = await fetch(apiUrl(`/masterData/qcItem/${qcItemId}`), {
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
		
		alert("저장되었습니다.");
	} catch (error) {
		console.log(error);
		alert(error.message || "저장에 실패했습니다.");
	}
}

async function getQcItemData() {
	const qcItemId = document.getElementById('modalQcItemId').value;
	const itemName = document.getElementById("itemName").value.trim();
	const targetType = document.getElementById("targetType").value;
	const unit = document.getElementById("unit").value;
	const minValue = document.getElementById("minValue").value;
	const maxValue = document.getElementById("maxValue").value;
	const useYn = document.getElementById("useYn").value;
	const sortOrder = document.getElementById("sortOrder").value;
	
	if (!isEdit && !qcItemId) {
		alert("QC ITEM ID는 필수입니다.");
		return null;
	}
	
	// 필수값 검증
	if (!itemName || !targetType || !unit || !useYn) {
		alert("필수 항목을 입력해주세요.");
		return null;
	}
	
	// 숫자 검증
	if (sortOrder === "" || isNaN(sortOrder)) {
		alert("정렬 순서를 숫자여야 합니다.");
		return;
	}
	
	if (minValue !== "" && isNaN(minValue)) {
		alert("최소값은 숫자여야 합니다.");
		return null;
	}

	if (maxValue !== "" && isNaN(maxValue)) {
		alert("최대값은 숫자여야 합니다.");
		return null;
	}

	if (minValue !== "" && maxValue !== "" && Number(minValue) > Number(maxValue)) {
		alert("최소값은 최대값보다 클 수 없습니다.");
		return null;
	}
	
	// sortOrder 중복 검증
	if (isDuplicateSortOrder(sortOrder, qcItemId)) {
		alert("이미 사용 중인 정렬 순서입니다.");
		return null;
	}
	
	if (!isEdit) {
		const isDuplicate = await checkQcItemId(qcItemId);
		if (isDuplicate) return null;
	}
	
	return {
		qcItemId,
		itemName,
		targetType,
		unit,
		minValue,
		maxValue,
		useYn,
		sortOrder
	}
}

function isDuplicateSortOrder(sortOrder, currentId) {
	return qcItemList.some(item => {
		if (currentId && item.qcItemId == currentId) {
			return false; // 자기 자신 제외
		}
		return Number(item.sortOrder) === Number(sortOrder);
	});
}

async function insertQcItem(data) {
	try {
		const res = await fetch(apiUrl(`/masterData/qcItem/add`), {
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
		
		alert("등록이 완료되었습니다.");
	} catch (error) {
		console.log(error);
		alert(error.message || "저장에 실패했습니다.");
	}
}

// QC ITEM ID 중복 검사 관련 로직
async function checkQcItemId(qcItemId) {
	try {
		const res = await fetch(apiUrl(`/masterData/qcItem/checkDuplicate?qcItemId=${qcItemId}`));
		const data = await res.json();
		
		if (data.isDuplicate) {
			alert("이미 사용 중인 ITEM ID 입니다.");
			return true;
		}
		
		return false;
		
	} catch (error) {
		console.error(error);
		alert("중복 검사에 실패했습니다.");
		return true; 
	}
}

// 대상 구분 조회
async function loadTargetType() {
	try {
		const res = await fetch(apiUrl("/commomCode/matType"));
		const data = await res.json();
		
		// select에서 보여질 내용
		targetTypeList = data.map(item => ({
			value: item.codeId,
			text: item.codeName
		}));
		
	} catch (e) {
		console.error(e);
	}
}

let searchKeyword = "";
// 검색 기능
document.getElementById("qcItemKeyword").addEventListener("input", (e) => {
	searchKeyword = e.target.value;
});

// 검색 버튼 이벤트
document.getElementById("searchbtn").addEventListener("click", () => {
	const keyword = searchKeyword.trim().toLowerCase();
	
	// 검색어가 없으면 빈 화면 보여주기
	if (!keyword) {
		qcItemGrid.resetData(qcItemList);
		return;
	}
	
	const filterData = qcItemList.filter(item => {
		const qcItemId = item.qcItemId ? item.qcItemId.toLowerCase() : "";
		const itemName = item.itemName ? item.itemName.toLowerCase() : "";
		
		return qcItemId.includes(keyword) || itemName.includes(keyword);
	});

	qcItemGrid.resetData(filterData);
});
