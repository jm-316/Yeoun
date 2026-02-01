let processCodeList = [];
let processTypeList = [];

const processCodeGrid = new tui.Grid({
	el: document.getElementById("processCodeGrid"),
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
			header: "공정ID",
			name: "processId",
			editor: "text",
			sortable: true,
		},
		{
			header: "공정명",
			name: "processName",
			editor: "text",
			filter: "select",
		},
		{
			header: "공정유형",
			name: "processType",
			filter: "select",
			editor: {
				type: "select",
				options: {
					listItems: []
				}
			},
			editable: ({row}) => {
				return isNewRow(row);
			},
		},
		{
			header: "설명",
			name: "description",
			editor: "text",
		},
		{
			header: "공정순서",
			name: "stepNo",
			editor: "text",
			renderer:{ type: StatusModifiedRenderer}
		},
		{
			header: "사용여부",
			name: "useYn",
			filter: "select",
			renderer:{ type: StatusModifiedRenderer},
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

// 기존 행 수정을 막을 때 사용
function isNewRow(rowKey) {
	const { createdRows = [] } = processCodeGrid.getModifiedRows();
	return createdRows.some(r => r.rowKey === rowKey);
}

// 변경하기 전 값
const beforeEditValues = {};

processCodeGrid.on("editingStart", ev => {
	const { rowKey, columnName } = ev;
	



	// processType 컬럼에 대해 기존 행 수정을 막는 로직
	    if (columnName === 'processType') {
	        if (!isNewRow(rowKey)) {
	            ev.stop(); // 신규 행이 아니면 편집 차단
	            return;
	        }
	    }

	beforeEditValues[rowKey] ??= {};
	beforeEditValues[rowKey][columnName] =
	    processCodeGrid.getValue(rowKey, columnName);
});

const validationRules = {
	processId: {
		required: true,
		pattern: /^[A-Za-z0-9\-_]{3,20}$/,
		errorMessage: "공정 코드는 영문 대문자와 숫자 3~10자만 가능합니다.",
		checkDuplicate: true
	},
	processName: {
		required: true,
		pattern: /^[가-힣a-zA-Z0-9\s~!@#$%^&*\(\)_+\-=\[\];:'",.<>/?]+$/,
		errorMessage: "공정명은 한글, 영문, 숫자 2~50자만 가능합니다.",
		checkDuplicate: true
	},
	stepNo: {
		required: true,
		pattern: /^\d+$/,
		errorMessage: "숫자만 입력 가능합니다.",
		min: 0,
		checkDuplicate: true
	},
	processType: {
		required: true,
		errorMessage: "공정유형을 선택해야합니다.",
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
	if (rule.min !== undefined) {
		const num = Number(stringValue);
		
		if (num < rule.min) {
			alert(`0 이상의 값을 입력하세요.`);
			restoreValue(rowKey, columnName);
			return false;
		}
	}
	
	// 중복 체크
	if (rule.checkDuplicate) {
		const isDuplicate = processCodeGrid.getData().some((row) => {
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
		processCodeGrid.setValue(rowKey, columnName, beforeValue);
	}, 0);
}

processCodeGrid.on("editingFinish", async (ev) => {
    const { rowKey, columnName, value } = ev;
	
	// 통합 검증
	if (!validateField(rowKey, columnName, value)) return;
	
	// 기타 컬럼 (select 관련 로직)
  	if (value === null || value === undefined || value === '') {
      const beforeValue = beforeEditValues?.[rowKey]?.[columnName];

      if (beforeValue !== undefined) {
          setTimeout(() => {
              processCodeGrid.setValue(rowKey, columnName, beforeValue);
          }, 0);
      }
  	}
});

// 컬럼 헤더명 가져오기
function getColumnHeader(columnName) {
	const column = processCodeGrid.getColumns().find(col => col.name === columnName);
	return column ? column.header : columnName;
}

// 클릭 동작
processCodeGrid.on('click', ev => {
	
    if (!ev.rowKey) return;
	
    if (ev.columnName === "useYn" || ev.columnName === "stepNo") {
        processCodeGrid.startEditing(ev.rowKey, ev.columnName);
    }
});

// 공정코드 정보 불러오기
async function loadProcessCode(useYn) {
	try {
		const res = await fetch(`/processMst/processCodes?useYn=${useYn}`);
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패");
		}
		
		const data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			processCodeGrid.resetData([]);
		}
		
		processCodeGrid.resetData(data);
		
		processCodeList = data;
	} catch (error) {
		console.error(error);
	}
}

// 그리드 컬럼 옵션 업데이트 함수
function updateGridColumnOptions() {
	const columns = processCodeGrid.getColumns();
	const processTypeColumn = columns.find(col => col.name === 'processType');
	
	if (processTypeColumn && processTypeColumn.editor) {
		processTypeColumn.editor.options.listItems = processTypeList;
	}
	
	processCodeGrid.setColumns(columns);
}


// 공통코드에서 공정유형 가져오기
async function loadprocessTypeCode() {
	try {
		const res = await fetch("/commomCode/processType");
		const data = await res.json();
		
		
		// select에서 보여질 내용
		processTypeList = data.map(item => ({
			value: item.codeId,
			text: item.codeId
		}));
		
		updateGridColumnOptions();
		
	} catch (e) {
		console.error(e);
	}
}

// =====================================================
// 추가 버튼 이벤트
document.getElementById("processCodeRegistBtn").addEventListener("click", () => {
	processCodeGrid.prependRow();
});

// 저장 버튼 이벤트
document.getElementById("processCodeSaveBtn").addEventListener("click", async () => {
	
	// 편집 완료
	processCodeGrid.finishEditing();
	
	const modifiedData = processCodeGrid.getModifiedRows() || {};
	const updateRows = modifiedData.updatedRows || [];
	let createdRows = modifiedData.createdRows || [];
	
	const isEmptyRow = (row) => {
	    return !row.processId && !row.processName && !row.processType && !row.stepNo;
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
	
//	showSpinner();
	
	await saveProcessCode(saveData);
		
//	hideSpinner();
});

// 원재료 등록
async function saveProcessCode(data) {
	try {
		const res = await fetch(apiUrl("/processMst/processCode/modify"), {
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
		
		await loadProcessCode("all"); // 데이터 재조회
		
		alert("저장되었습니다.");
		
	} catch (e) {
		console.error(e);
		alert(e.message || "저장에 실패했습니다.");
		await loadProcessCode("all");  // 데이터 재조회
	}
}

let searchMatKeyword = "";

// 검색어 입력 이벤트
document.getElementById("processCodeKeyword").addEventListener("input", (e) => {
	searchMatKeyword = e.target.value;
});

// 검색 버튼 이벤트
document.getElementById("processCodeSearch").addEventListener("click", () => {
	const keyword = searchMatKeyword.trim().toLowerCase();
	
	if (!keyword) {
		processCodeGrid.resetData(processCodeList);
		return;
	}
	
	const filterData = processCodeList.filter(item => {
		const processId = item.processId ? item.processId.toLowerCase() : "";
		const processName = item.processName ? item.processName.toLowerCase() : "";
		
		return processId.includes(keyword) || processName.includes(keyword);
	});
	
	processCodeGrid.resetData(filterData);
});

//function showSpinner() {
//	document.getElementById('loading-overlay').style.display = 'flex';
//}
//function hideSpinner() {
//	document.getElementById('loading-overlay').style.display = 'none';
//}

// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async (e) => {
	await loadprocessTypeCode();
	await loadProcessCode("all"); // 공정 코드 목록 조회
	
});
