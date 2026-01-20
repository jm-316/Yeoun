let matTypeList = []; // 원재료 타입 리스트 (드롭다운에 사용)
let unitList = []; // 원재료 단위 리스트 (드롭다운에 사용)
let matTypeMap = {};

// effectiveDate 허용범위 (개월 단위)
const EFFECTIVE_DATE_MIN = 0;
const EFFECTIVE_DATE_MAX = 120; // 예: 최대 120개월(10년)

const materialGrid = new tui.Grid({
	el: document.getElementById("materialGrid"),
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
			header: "원재료코드",
			name: "matCode",
			editor: "text"
		},
		{
			header: "원재료명",
			name: "matName",
			editor: "text"
		},
		{
			header: "타입",
			name: "matType",
			editor: {
				type: 'select', // 드롭다운 사용
				options: {
					listItems: []
				}
			},
			formatter: ({value}) => {
				const type = matTypeList.find(item => item.value === value);
				return type ? type.text : value;
			}
		},
		{
			header: "단위",
			name: "matUnit",
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
const beforeEditValues = {};

materialGrid.on("editingStart", ev => {
    const { rowKey, columnName } = ev;

    beforeEditValues[rowKey] ??= {};
    beforeEditValues[rowKey][columnName] =
        materialGrid.getValue(rowKey, columnName);
});

const validationRules = {
	matCode: {
		required: true,
		pattern: /^[A-Za-z0-9\-_]{3,20}$/,
		errorMessage: "원재료 코드는 영문 대문자와 숫자 3~10자만 가능합니다.",
		checkDuplicate: true
	},
	matName: {
		required: true,
		pattern: /^[가-힣a-zA-Z0-9\s~!@#$%^&*\(\)_+\-=\[\];:'",.<>/?]+$/,
		errorMessage: "원재료명은 한글, 영문, 숫자 2~50자만 가능합니다.",
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
function validateField(rowKey, columnName, value) {
	const rule = validationRules[columnName];
	
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
		materialGrid.setValue(rowKey, columnName, beforeValue);
	}, 0);
}


materialGrid.on("editingFinish", ev => {
    const { rowKey, columnName, value } = ev;
	
	// 통합 검증
	if (!validateField(rowKey, columnName, value)) return;
	
	// 기타 컬럼 (select 관련 로직)
  	if (value === null || value === undefined || value === '') {
      const beforeValue = beforeEditValues?.[rowKey]?.[columnName];

      if (beforeValue !== undefined) {
          setTimeout(() => {
              materialGrid.setValue(rowKey, columnName, beforeValue);
          }, 0);
      }
  	}
});

// 컬럼 헤더명 가져오기
function getColumnHeader(columnName) {
	const column = materialGrid.getColumns().find(col => col.name === columnName);
	return column ? column.header : columnName;
}

// 데이터 변경 감지
//materialGrid.on("afterChange", ev => {
//	ev.changes.forEach(change => {
//		const { rowKey, columnName, prevValue  } = change;
//		
////		materialGrid.setValue(rowKey, columnName, prevValue);
//	});
//});

// 클릭 동작
materialGrid.on('click', ev => {
	
    if (!ev.rowKey) return;

    if (ev.columnName === "useYn") {
        materialGrid.startEditing(ev.rowKey, ev.columnName);
    }
	
	if (ev.columnName === "matUnit") {
	    materialGrid.startEditing(ev.rowKey, ev.columnName);
	}
	
	if (ev.columnName === "matType") {
	    materialGrid.startEditing(ev.rowKey, ev.columnName);
	}
	
	if (ev.columnName === "effectiveDate") {
	    materialGrid.startEditing(ev.rowKey, ev.columnName);
	}
});

// 원재료 정보 불러오기
async function loadMaterial(useYn) {
	const MATERIAL_LIST = `/masterData1/data/materialList?useYn=${useYn}`;
			
	try {
		const res = await fetch(MATERIAL_LIST, {method: "GET"});
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}
		
		const data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			materialGrid.resetData([]);
		}

		materialGrid.resetData(data);
		
	} catch (error) {
		console.error(error);
	}
}

// 공통코드에서 원재료 타입 가져오기
async function loadMatTypeCode() {
	const MATERIAL_TYPE_URL = "/commomCode/matType";
	
	try {
		const res = await fetch(MATERIAL_TYPE_URL);
		let data = await res.json();
		
		data = data.filter(item => item.codeId !== "WIP" && item.codeId !== "FIN")
		
		// select에서 보여질 내용
		matTypeList = data.map(item => ({
			value: item.codeId,
			text: item.codeName
		}));
		
		// 영어를 한글로 변환할 때 사용
		matTypeMap = data.reduce((acc, cur) => {
			acc[cur.codeId] = cur.codeName;
			return acc;
		}, {});
		
		updateGridColumnOptions();
		
	} catch (e) {
		console.error(e);
	}
}

// 공통코드에서 단위 가져오기
async function loadUnit() {
	const MATERIAL_UNIT_URL = "/commomCode/unit";
	
	try {
		const res = await fetch(MATERIAL_UNIT_URL);
		const data = await res.json();
		
		// select에서 보여질 내용
		unitList = data.map(item => ({
			value: item.codeId,
			text: item.codeName
		}));
		
		updateGridColumnOptions();
		
	} catch (e) {
		console.error(e);
	}
}

// 그리드 컬럼 옵션 업데이트 함수
function updateGridColumnOptions() {
	const columns = materialGrid.getColumns();
	const matTypeColumn = columns.find(col => col.name === 'matType');
	const matUnitColumn = columns.find(col => col.name === 'matUnit');
	
	if (matTypeColumn && matTypeColumn.editor) {
		matTypeColumn.editor.options.listItems = matTypeList;
	}
	
	if (matUnitColumn && matUnitColumn.editor) {
		matUnitColumn.editor.options.listItems = unitList;
	}
	
	materialGrid.setColumns(columns);
}


// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async (e) => {
	await loadMatTypeCode(); // 공통코드에서 원재료 타입 조회
	await loadUnit(); // 공통코드에서 단위 조회
	await loadMaterial("all"); // 원재료 목록 조회
	

	//스피너  off
	hideSpinner();
});

// 추가 버튼 이벤트
document.getElementById("matRegistBtn").addEventListener("click", () => {
	materialGrid.prependRow();
});


// 저장 버튼 이벤트
document.getElementById("matSaveBtn").addEventListener("click", async () => {
	
	// 편집 완료
	materialGrid.finishEditing();
	
	const modifiedData = materialGrid.getModifiedRows() || {};
	const updateRows = modifiedData.updatedRows || [];
	let createdRows = modifiedData.createdRows || [];
	
	const isEmptyRow = (row) => {
	    return !row.matCode && !row.matName && !row.matType && !row.matUnit;
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
	
	await saveMaterial(saveData);
	alert("저장되었습니다.");
	
	await loadMaterial(); // 데이터 재조회
	
	hideSpinner();
});

// 원재료 등록
async function saveMaterial(data) {
	const MATERIAL_ADD_URL = "/masterData1/data/material/add";
	
	try {
		const res = await fetch(apiUrl(MATERIAL_ADD_URL), {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data)
		});
		
		if (!res.ok) {
			   throw new Error(`등록 실패: ${res.status}`);
		}
		
	} catch (e) {
		console.error(e);
		alert("저장에 실패했습니다.")
	}
}


function showSpinner() {
	document.getElementById('loading-overlay').style.display = 'flex';
}
function hideSpinner() {
	document.getElementById('loading-overlay').style.display = 'none';
}
