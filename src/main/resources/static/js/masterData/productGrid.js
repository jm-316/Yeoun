let productDataList = [];
let prdTypeList = []; // 완제품 타입 리스트 (드롭다운에 사용)
let prdUnitList = []; // 완제품 단위 리스트 (드롭다운에 사용)
let prdTypeMap = {};

// effectiveDate 허용범위 (개월 단위)
const EFFECTIVE_DATE_MIN = 0;
const EFFECTIVE_DATE_MAX = 120; // 예: 최대 120개월(10년)

const productGrid = new tui.Grid({
	el: document.getElementById("productGrid"),
	bodyHeight: 500,
	rowHeaders: ['rowNum'],
	pageOptions: {
	    useClient: true,  // 클라이언트 사이드 페이징
	    perPage: 20       // 페이지당 20개 행
	},	
	columnOptions: {
		resizable: true
	},
	editingEvent: 'click',
	columns: [
		{
			header: "완제품코드",
			name: "prdCode",
			editor: "text",
			sortable: true
		},
		{
			header: "완제품명",
			name: "prdName",
			editor: "text",
			sortable: true
		},
		{
			header: "타입",
			name: "prdType",
			filter: "select",
			editor: {
				type: 'select', // 드롭다운 사용
				options: {
					listItems: []
				}
			},
			formatter: ({value}) => {
				const type = prdTypeList.find(item => item.value === value);
				return type ? type.text : value;
			}
		},
		{
			header: "단위",
			name: "prdUnit",
			editor: {
				type: 'select', // 드롭다운 사용
				options: {
					listItems: []
				}
			},
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
const beforeEditPrdValues = {};

productGrid.on("editingStart", ev => {
    const { rowKey, columnName } = ev;
	
    beforeEditPrdValues[rowKey] ??= {};
    beforeEditPrdValues[rowKey][columnName] =
        productGrid.getValue(rowKey, columnName);
});

// 완제품 코드 수정 가능 여부 확인
async function checkPrdCode(prdCode) {
	try {
		const CHECK_PRD_CODE_URL = `/masterData1/data/check/prd?prdCode=${prdCode}`
		
		const res = await fetch(CHECK_PRD_CODE_URL);
		const data = await res.json();
		
		return {
			allowed: !data.isUsed,
			message: data.isUsed 
               ? "BOM에 등록 또는 재고가 있는 완제품는 코드를 변경할 수 없습니다."
               : null
		};
	} catch (error) {
		console.error(error);
		alert("완제품 코드 검사에 실패했습니다.");
	}
}

const validationPrdRules = {
	prdCode: {
		required: true,
		pattern: /^[A-Za-z0-9\-_]{3,20}$/,
		errorMessage: "완제품 코드는 영문 대문자와 숫자 3~10자만 가능합니다.",
		checkDuplicate: true
	},
	prdName: {
		required: true,
		pattern: /^[가-힣a-zA-Z0-9\s~!@#$%^&*\(\)_+\-=\[\];:'",.<>/?]+$/,
		errorMessage: "완제품명은 한글, 영문, 숫자 2~50자만 가능합니다.",
		checkDuplicate: true
	},
	effectiveDate: {
		required: false,
		pattern: /^\d+$/,
		errorMessage: "숫자만 입력 가능합니다.",
		min: EFFECTIVE_DATE_MIN,
		max: EFFECTIVE_DATE_MAX
	}
}

// 통합 유효성 검사
function validatePrdField(rowKey, columnName, value) {
	const rule = validationPrdRules[columnName];
	
	// 검증 규칙이 없으면 통과
	if (!rule) {
		return true;
	}
	
	// 필수 체크
	if (rule.required && (!value || value === "")) {
		alert(`${getColumnHeader(columnName)}은(는) 필수입니다.`);
		restoreValue(rowKey, columnName);
		return false;
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
	
	// 범위 체크
	if (rule.min !== undefined || rule.max !== undefined) {
		const num = Number(stringValue);
		
		if (num < rule.min || num > rule.max) {
			alert(`${rule.min}~${rule.max} 사이의 값을 입력하세요.`);
			restoreValue(rowKey, columnName);
			return false;
		}
	}
	
	// 중복 체크
	if (rule.checkDuplicate) {
		const isDuplicate = productGrid.getData().some((row) => {
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
	const beforeValue = beforeEditPrdValues?.[rowKey]?.[columnName] || "";
	setTimeout(() => {
		productGrid.setValue(rowKey, columnName, beforeValue);
	}, 0);
}


productGrid.on("editingFinish", async (ev) => {
    const { rowKey, columnName, value } = ev;
	
	// 통합 검증
	if (!validatePrdField(rowKey, columnName, value)) return;
	
	if (columnName === "prdCode") {
		const rowData = productGrid.getRow(rowKey);
		const prdCode = rowData.prdCode;
		const canEdit = await checkPrdCode(prdCode);
		
		if (!canEdit.allowed) {
			alert(canEdit.message);
			ev.stop();
			return;
		}
	}
	
	// 기타 컬럼 (select 관련 로직)
  	if (value === null || value === undefined || value === '') {
      const beforeValue = beforeEditPrdValues?.[rowKey]?.[columnName];

      if (beforeValue !== undefined) {
          setTimeout(() => {
              productGrid.setValue(rowKey, columnName, beforeValue);
          }, 0);
      }
  	}
});

// 컬럼 헤더명 가져오기
function getColumnHeader(columnName) {
	const column = productGrid.getColumns().find(col => col.name === columnName);
	return column ? column.header : columnName;
}

// 클릭 동작
productGrid.on('click', ev => {
	
    if (!ev.rowKey) return;

    if (ev.columnName === "useYn") {
        productGrid.startEditing(ev.rowKey, ev.columnName);
    }
	
	if (ev.columnName === "matUnit") {
	    productGrid.startEditing(ev.rowKey, ev.columnName);
	}
	
	if (ev.columnName === "matType") {
	    productGrid.startEditing(ev.rowKey, ev.columnName);
	}
	
	if (ev.columnName === "effectiveDate") {
	    productGrid.startEditing(ev.rowKey, ev.columnName);
	}
});

// 원재료 정보 불러오기
async function loadProduct(useYn) {
	const PRODUCT_LIST = `/masterData1/data/productList?useYn=${useYn}`;
			
	try {
		const res = await fetch(PRODUCT_LIST, {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		const data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			productGrid.resetData([]);
		}

		productGrid.resetData(data);
		productDataList = data;
		
	} catch (error) {
		console.error(error);
	}
}

// 공통코드에서 완제품 타입 가져오기
async function loadPrdTypeCode() {
	const PRODUCT_TYPE_URL = "/commomCode/prdType";
	
	try {
		const res = await fetch(PRODUCT_TYPE_URL);
		let data = await res.json();
		
		// select에서 보여질 내용
		prdTypeList = data.map(item => ({
			value: item.codeId,
			text: item.codeName
		}));
		
		// 영어를 한글로 변환할 때 사용
		prdTypeMap = data.reduce((acc, cur) => {
			acc[cur.codeId] = cur.codeName;
			return acc;
		}, {});
		
		updateProductGridColumnOptions();
		
	} catch (e) {
		console.error(e);
	}
}

// 공통코드에서 단위 가져오기
async function loadPrdUnit() {
	const PRODUCT_UNIT_URL = "/commomCode/unit";
	
	try {
		const res = await fetch(PRODUCT_UNIT_URL);
		const data = await res.json();
		
		// select에서 보여질 내용
		prdUnitList = data.map(item => ({
			value: item.codeId,
			text: item.codeName
		}));
		
		updateProductGridColumnOptions();
		
	} catch (e) {
		console.error(e);
	}
}

// 그리드 컬럼 옵션 업데이트 함수
function updateProductGridColumnOptions() {
	const columns = productGrid.getColumns();
	const prdTypeColumn = columns.find(col => col.name === 'prdType');
	const prdUnitColumn = columns.find(col => col.name === 'prdUnit');
	
	if (prdTypeColumn && prdTypeColumn.editor) {
		prdTypeColumn.editor.options.listItems = prdTypeList;
	}
	
	if (prdUnitColumn && prdUnitColumn.editor) {
		prdUnitColumn.editor.options.listItems = prdUnitList;
	}
	
	productGrid.setColumns(columns);
}


// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async (e) => {
	await loadPrdTypeCode(); // 공통코드에서 원재료 타입 조회
	await loadPrdUnit(); // 공통코드에서 단위 조회
	await loadProduct("all"); // 원재료 목록 조회
	

	//스피너  off
	hideSpinner();
});

// 검색어 입력
let searchPrdKeyword = "";
document.getElementById("productKeyword").addEventListener("input", (e) => {
	searchPrdKeyword = e.target.value;
});

// 검색 버튼 이벤트
document.getElementById("productSearch").addEventListener("click", () => {
	const keyword = searchPrdKeyword.trim().toLowerCase();
	
	// 검색어가 없으면 빈 화면 보여주기
	if (!keyword) {
		productGrid.resetData(productDataList);
		return;
	}
	
	const filterData = productDataList.filter(item => {
		const prdName = item.prdName ? item.prdName.toLowerCase() : "";
		const prdCode = item.prdCode ? item.prdCode.toLowerCase() : "";
		
		return prdName.includes(keyword) || prdCode.includes(keyword);
	});
	
	productGrid.resetData(filterData);
});

// 추가 버튼 이벤트
document.getElementById("prdRegistBtn").addEventListener("click", () => {
	productGrid.prependRow();
});


// 저장 버튼 이벤트
document.getElementById("prdSaveBtn").addEventListener("click", async () => {
	
	// 편집 완료
	productGrid.finishEditing();
	
	const modifiedData = productGrid.getModifiedRows() || {};
	const updateRows = modifiedData.updatedRows || [];
	let createdRows = modifiedData.createdRows || [];
	
	const isEmptyRow = (row) => {
	    return !row.prdCode && !row.prdName && !row.prdType && !row.prdUnit;
	};
	
	createdRows = createdRows.filter(row => !isEmptyRow(row));
		
	if (updateRows.length === 0 && createdRows.length === 0) {
		alert("수정된 내용이 없습니다.");
		return;
	}
	
	// 모든 변경사항 저장할 객체
	const saveData = {
		created: createdRows,
		updated: updateRows
	}
	
	showSpinner();
	
	await saveProduct(saveData);
	
	hideSpinner();
});

// 원재료 등록
async function saveProduct(data) {
	const PRODUCT_ADD_URL = "/masterData1/data/product/add";
	
	try {
		const res = await fetch(apiUrl(PRODUCT_ADD_URL), {
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
		
		alert("저장이 완료되었습니다.");
		
		await loadProduct("all"); // 데이터 재조회
		
	} catch (e) {
		console.error(e);
		alert(e.message || "저장에 실패했습니다.");
		await loadProduct("all"); // 데이터 재조회
	}
}

function showSpinner() {
	document.getElementById('loading-overlay').style.display = 'flex';
}
function hideSpinner() {
	document.getElementById('loading-overlay').style.display = 'none';
}
