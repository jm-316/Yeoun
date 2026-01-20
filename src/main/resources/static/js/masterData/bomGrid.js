let bomItem = [];

const bomGrid = new tui.Grid({
	el: document.getElementById("bomGrid"),
	bodyHeight: 500,
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
				header: "BOMID",
				name: "bomId",
				hidden: true
			},
			{
				header: "BOM이름",
				name: "bomName",
				editor: "text"
			},
			{
				header: "제품명",
				name: "prdName",
				editor: "text"
			},
			{
				header: "사용여부",
				name: "useYn",
				filter: "select",
				editor: {
					type: 'select', // 드롭다운 사용
					options: {
						// value는 실제 데이터 값, text는 사용자에게 보이는 값
						listItems: [
							{value: "Y", text: '활성'},
							{value: "N", text: '비활성'}
						]
					}
				},
				formatter: ({value}) => {
					const statusMap = {
						"Y": "활성",
						"N": "비활성"
					};
					return statusMap[value] || value;
				}
			}
		]
});

// 변경하기 전 값
const beforeEditValues = {};

bomGrid.on("editingStart", ev => {
    const { rowKey, columnName } = ev;

    beforeEditValues[rowKey] ??= {};
    beforeEditValues[rowKey][columnName] =
        bomGrid.getValue(rowKey, columnName);
});

const validationRules = {
	bomName: {
		required: true,
		pattern: /^[가-힣a-zA-Z0-9\s~!@#$%^&*\(\)_+\-=\[\];:'",.<>/?]+$/,
		errorMessage: "bom 이름은 한글, 영문, 숫자 2~50자만 가능합니다.",
		checkDuplicate: true
	}
}

// 통합 유효성 검사
function validateField(rowKey, columnName, value) {
	const rule = validationRules[columnName];
	
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

	// 중복 체크
	if (rule.checkDuplicate) {
		const isDuplicate = materialGrid.getData().some((row) => {
			return row[columnName] === stringValue && row.rowKey !== rowKey;
		});
	
		if (isDuplicate) {
			alert(`이미 존재하는 ${getColumnHeader(columnName)}입니다.`);
			restoreValue(rowKey, columnName);
			return false;
		}
	}
	
	return true;
}

// 이전 값으로 복원
function restoreValue(rowKey, columnName) {
	const beforeValue = beforeEditValues?.[rowKey]?.[columnName] || "";
	setTimeout(() => {
		bomGrid.setValue(rowKey, columnName, beforeValue);
	}, 0);
}

bomGrid.on("editingFinish", ev => {
    const { rowKey, columnName, value } = ev;
	
	// 통합 검증
	if (!validateField(rowKey, columnName, value)) return;
	
	// 기타 컬럼 (select 관련 로직)
  	if (value === null || value === undefined || value === '') {
      const beforeValue = beforeEditValues?.[rowKey]?.[columnName];

      if (beforeValue !== undefined) {
          setTimeout(() => {
              bomGrid.setValue(rowKey, columnName, beforeValue);
          }, 0);
      }
  	}
});

// 컬럼 헤더명 가져오기
function getColumnHeader(columnName) {
	const column = bomGrid.getColumns().find(col => col.name === columnName);
	return column ? column.header : columnName;
}

let isSelect = false;

// 클릭 동작
bomGrid.on('click', async (ev) => {
    if (ev.rowKey == null) return;
	
    if (ev.columnName === "useYn") {
        bomGrid.startEditing(ev.rowKey, ev.columnName);
		return;
    }
	const rowData = bomGrid.getRow(ev.rowKey);
	const bomId = rowData.bomId; // 숨김 컬럼에서 bomId 가져오기
	
	if (bomId) {
		isSelect = true;
		await loadBomItem(bomId);
	}
});

// bom 정보 불러오기
async function loadBom() {
	const BOM_LIST = "/bomMst/bomList";
			
	try {
		const res = await fetch(BOM_LIST, {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		const data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			bomGrid.resetData([]);
		}

		bomGrid.resetData(data);
		
	} catch (error) {
		console.error(error);
	}
}

// ==================================================================
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
//			
		} catch (error) {
			console.error(error);
		}
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
		bomItemGrid.prependRow();
	}
});

// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async () => {
	await loadBom(); // bom 목록 조회

	//스피너  off
//	hideSpinner();
});
