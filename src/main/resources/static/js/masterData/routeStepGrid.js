let routeStepList = [];

const routeStepGrid = new tui.Grid({
	el: document.getElementById("routeStepGrid"),
	bodyHeight: 300,
	rowHeaders: ['rowNum'],
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
			header: "라우트단계ID",
			name: "routeStepId",
		},
		{
			header: "라우트ID",
			name: "routeId",
		},
		{
			header: "순서",
			name: "stepSeq",
			editor: "text"
		},
		{
			header: "공정ID",
			name: "processId",
		},
		{
			header: "공정명",
			name: "remark",
		},
		{
			header: "QC여부",
			name: "qcPointYn",
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

const processCodeModal = new bootstrap.Modal(document.getElementById("processLookup-modal"));

routeStepGrid.on("afterChange", () => {
	stepChanged = true;
});

routeStepGrid.on('click', async (ev) => {
	const { columnName, rowKey } = ev;
	
	const rowData = routeStepGrid.getRow(rowKey);
	
	if ((columnName === "processId" || columnName === "remark")&& rowData.routeStepId === null) {
		try {
			// 원재료 모달 열기
			processCodeModal.show();
			
			// 현재 선택된 row 정보 저장
			window.selectedBomRowKey = rowKey;
			
			const modalElement = document.getElementById('processLookup-modal');
	        	modalElement.addEventListener('shown.bs.modal', async function loadData() {
	            	await loadProcessCode("Y");
	              
	              	// 그리드 강제 리프레시
	              	routeStepCodeGrid.refreshLayout();
	              
	              	// 이벤트 리스너 제거 (한 번만 실행되도록)
	              	modalElement.removeEventListener('shown.bs.modal', loadData);
	          }, { once: true }); // once 옵션으로 자동 제거
		} catch (error) {
			console.error(error);
		}
	}
});

// ROUTE_STEP 조회
async function loadRouteStep(useYn, routeId) {
	try {
		const res = await fetch(apiUrl(`/processMst/routeStep?useYn=${useYn}&routeId=${routeId}`));
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패");
		}
		
		const data = await res.json();
		
		if (!data || data.length === 0) {
			routeStepGrid.resetData([]);
		}
		
		routeStepGrid.resetData(data);
		
		routeStepList = data;
	} catch (error) {
		console.error(error);
	}
}

function collectRouteStepData() {
	const modifiedData = routeStepGrid.getModifiedRows() || {};
	const updateRows = modifiedData.updatedRows || [];
	let createdRows = modifiedData.createdRows || [];
	
	const isEmptyRow = (row) => {
		return !row.stepSeq && !row.processId && !row.useYn;
	}

	createdRows = createdRows.filter(row => !isEmptyRow(row));
	
	stepChanged = true;
	
	return {
		created: createdRows,
		updated: updateRows
	}
	
}

// ROUTE_STEP 변경 사항 저장
async function saveSteps(data, routeId) {
	try {
		const res = await fetch(apiUrl(`/processMst/routeStep/modify/${routeId}`), {
			method: 'POST',
			headers: {
				[csrfHeader]: csrfToken,
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data)
		});
		
		if (!res.ok) {
		    let message = `저장 실패 (${res.status})`;

		    try {
		        const errorText = await res.text();
				if (errorText) {
				    message = errorText;
				}
		    } catch (_) {}

		    throw new Error(message);
		}
		
		stepChanged = false;
	} catch (error) {
		console.error(error);
		alert(error.message || "저장에 실패했습니다.");
	}
}

function validateSteps() {
	const rows = routeStepGrid.getData();

	if (rows.length === 0) {
		alert("라우트 단계는 최소 1개 이상 필요합니다.");
		return false;
	}
	
	const stepSeqSet = new Set();

		for (let i = 0; i < rows.length; i++) {
			const row = rows[i];
			const index = i + 1;

			// 필수값 체크
			if (row.stepSeq == null || row.stepSeq === "") {
				alert(`${index}번째 단계의 순서를 입력하세요.`);
				return false;
			}

			if (!row.processId) {
				alert(`${index}번째 단계에 공정이 지정되지 않았습니다.`);
				return false;
			}

			if (!row.qcPointYn) {
				alert(`${index}번째 단계의 QC 여부를 선택하세요.`);
				return false;
			}

			if (!row.useYn) {
				alert(`${index}번째 단계의 사용여부를 선택하세요.`);
				return false;
			}

			// 숫자 체크
			if (isNaN(row.stepSeq)) {
				alert(`${index}번째 단계의 순서는 숫자여야 합니다.`);
				return false;
			}

			// 중복 체크
			if (stepSeqSet.has(row.stepSeq)) {
				alert(`순서 ${row.stepSeq}번이 중복되었습니다.`);
				return false;
			}

			stepSeqSet.add(row.stepSeq);
		}

		return true;
	}
