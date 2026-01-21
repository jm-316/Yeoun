let bomItem = [];
let bomList = [];
let selectedBomId = null;
let targetGrid = null;

const productItemModal = new bootstrap.Modal(document.getElementById("products-modal"));

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
		selectedBomId = bomId;
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
		
		bomList = data;
		
	} catch (error) {
		console.error(error);
	}
}

// =============================================
// bom 등록

const bomModal = new bootstrap.Modal(document.getElementById("bom-modal"));

const registBomItemGrid = new tui.Grid({
	el : document.getElementById("registBomItemGrid"),
	rowHeaders: ['checkbox'],
	pageOptions: {
	    useClient: true,  // 클라이언트 사이드 페이징
	    perPage: 10       // 페이지당 10개 행
	},	
	columnOptions: {
		resizable: true
	},
	editingEvent: 'click',
	columns: [
		{
			header: "MATID",
			name: "matId",
			hidden: true
		},
		{
			header: "원재료코드",
			name: "matCode",
		},
		{
			header: "원재료명",
			name: "matName",
		},
		{
			header: "타입",
			name: "matType",
			formatter: ({value}) => {
				const type = matTypeList.find(item => item.value === value);
				return type ? type.text : value;
			}
		},
		{
			header: "단위",
			name: "matUnit",
		},
		{
			header: "유효일자(개월)",
			name: "effectiveDate",
			editor: {
				type: "text",
			},
			formatter: ({value}) => {
				
				if (value !== null) {
					if (value > 0) {
						return `${value}개월`
					} else {
						return "유통기한없음"
					}
				} else {
					return ""
				}
				
			}
		},
	]
});

registBomItemGrid.on("click", async (ev) => {
	const { columnName, rowKey } = ev;
	
	console.log(columnName);
	
	if (columnName === "matName" || columnName === "matCode") {
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
	}
});

document.getElementById("bomRegistBtn").addEventListener("click", () => {
	bomModal.show();
	
	const modalElement = document.getElementById('bom-modal');
		modalElement.addEventListener('shown.bs.modal', async function loadData() {
	    	await loadMaterial("Y");
	      
	      	// 그리드 강제 리프레시
	      	registBomItemGrid.refreshLayout();
	      
	      	// 이벤트 리스너 제거 (한 번만 실행되도록)
	      	modalElement.removeEventListener('shown.bs.modal', loadData);
	  }, { once: true }); // once 옵션으로 자동 제거
});

// 원재료 추가 버튼 이벤트
document.getElementById("registBtn").addEventListener("click", () => {
	registBomItemGrid.prependRow();
	targetGrid = "registBom";
});

// bom name 입력 이벤트
document.getElementById("bomName").addEventListener("input", async (e) => {
	const bomName = e.target.value.trim();
	
	await checkBomName(bomName);
});

// 완제품 조회 이벤트
document.getElementById("searchProduct").addEventListener("click", async () => {
	// 완제품 모달 열기
	productItemModal.show();
	
	const modalElement = document.getElementById('products-modal');
		modalElement.addEventListener('shown.bs.modal', async function loadData() {
	    	await loadProduct("Y");
	      
	      	// 그리드 강제 리프레시
	      	productItemGrid.refreshLayout();
	      
	      	// 이벤트 리스너 제거 (한 번만 실행되도록)
	      	modalElement.removeEventListener('shown.bs.modal', loadData);
	  }, { once: true }); // once 옵션으로 자동 제거
});

// 삭제 버튼 이벤트
document.getElementById("deleteBtn").addEventListener("click", () => {
	const checkedRows = registBomItemGrid.getCheckedRowKeys();
	
	if (checkedRows.length === 0) {
		alert("삭제할 원재료를 선택해주세요.");
		return;
	}
	
	registBomItemGrid.removeCheckedRows();
});

async function checkBomName(bomName) {
	const feedbackElement = document.getElementById("bomNameFeedback");
	
	try {
		const CHECK_BOM_NAME_URL = `/bomMst/data/checkDuplicate?bomName=${bomName}`;
		
		const res = await fetch(CHECK_BOM_NAME_URL);
		const data = await res.json();
		
		if (data.isDuplicate) {
			feedbackElement.innerHTML = '<span style="color: red;">이미 사용 중인 BOM 이름입니다.</span>';
		} else {
			feedbackElement.innerHTML = '<span style="color: green;">사용 가능한 BOM 이름입니다.</span>';
		}
		
	} catch (error) {
		console.log(error);
		alert("중복 검사에 실패했습니다.");
	}
}

// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async () => {
	await loadBom(); // bom 목록 조회

	//스피너  off
//	hideSpinner();
});

async function loadUnit(url) {
	
	try {
		const res = await fetch(url);
		const data = await res.json();
		
		// select에서 보여질 내용
		unitList = data.map(item => ({
			value: item.codeId,
			text: item.codeName
		}));
		
	} catch (e) {
		console.error(e);
	}
}
