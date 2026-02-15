let routeList = [];
let isEdit = false;
let headerChanged = false;
let stepChanged = false;

const routeHeader = {
	routeId: null,
	prdId: null,
	routeName: null,
	description: null,
	useYn: "Y"
}
const productItemModal = new bootstrap.Modal(document.getElementById("products-modal"));

const routeGrid = new tui.Grid({
	el: document.getElementById("routeGrid"),
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
			header: "라우트ID",
			name: "routeId",
			sortable: true,
		},
		{
			header: "제품코드",
			name: "prdId",
			filter: "select",
		},
		{
			header: "라우트명",
			name: "routeName",
			filter: "select",
		},
		{
			header: "설명",
			name: "description",
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
		}
	]
});

// 라우트 정보 불러오기
async function loadRoute() {
	try {
		const res = await fetch(apiUrl("/processMst/routes"));
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패");
		}
		
		const data = await res.json();
		
		if (!data || data.length === 0) {
			routeGrid.resetData([]);
		}
		
		routeGrid.resetData(data);
		
		routeList = data;
		
	} catch (error) {
		console.error(error);
		
	}
}

routeGrid.on("click", async (ev) => {
	const { targetType, columnName, rowKey } = ev;
	
	if (targetType === "cell" && columnName === "view_details") {
		const rowData = routeGrid.getRow(rowKey);
		const routeId = rowData.routeId;
		const route = await loadRouteDetail(routeId);
		
		isEdit = true;
		// 예: 모달 열기, 상세 정보 표시 등		
		$('#route-modal').modal('show');
		
		const modalTitle = document.getElementById("routeModalTitle");
		
		modalTitle.textContent = "라우트 상세";
		
		document.getElementById("prdCode").value = route.prdId;
		document.getElementById("searchProduct").disabled = true;
		
		window.selectedBomRowKey = rowKey;
		
		const modalElement = document.getElementById("route-modal");
			modalElement.addEventListener("shown.bs.modal", async function loadData() {
		    	await loadRouteStep("all", routeId);
				
				setHeaderData(routeHeader);
				fillModal(route);
		      	// 그리드 강제 리프레시
		      	routeStepGrid.refreshLayout();
		      
		      	// 이벤트 리스너 제거 (한 번만 실행되도록)
		      	modalElement.removeEventListener('shown.bs.modal', loadData);
		  }, { once: true }); // once 옵션으로 자동 제거
		
		routeHeader.routeId = routeId;
		
		await loadRoute();
	}
});

// route 상세 조회
async function loadRouteDetail(routeId) {
	try {
		const res = await fetch(apiUrl(`/processMst/route/${routeId}`));
		
		if (!res.ok) {
			throw new Error("상세 데이터 조회 실패");
		}
		
		const data = await res.json();
		
		return data;
	} catch(error) {
		console.error(error);
	}
}

// 모달 채우기
function fillModal(data) {
	document.getElementById('modalRouteId').value = data.routeId;//라우트 ID
	document.getElementById('prdCode').value = data.prdId;//제품코드
	document.getElementById('modalRouteName').value = data.routeName;//라우트명
	document.getElementById('modalRouteUseYn').value = data.useYn;//사용여부
	document.getElementById('modalRouteRemark').value = data.description;//비고
	
	document.getElementById('modalRouteId').readOnly = true;
}

function resetModal() {
	document.getElementById('modalRouteId').value = '';//라우트 ID
	document.getElementById('prdCode').value = '';//제품코드
	document.getElementById('modalRouteName').value = '';//라우트명
	document.getElementById('modalRouteUseYn').value = 'Y';//사용여부
	document.getElementById('modalRouteRemark').value = '';//비고
	
	document.getElementById('modalRouteId').readOnly = false;
	
	isEdit = false;
}

let initializing = false;

function setHeaderData(data) {
  initializing = true;

  document.getElementById("modalRouteId").value = data.routeId ?? "";
  document.getElementById("modalRouteName").value = data.routeName ?? "";
  document.getElementById("prdCode").value = data.prdId ?? "";
  document.getElementById("modalRouteRemark").value = data.description ?? "";
  document.getElementById("modalRouteUseYn").value = data.useYn ?? "Y";

  routeHeader.routeId = data.routeId ?? "";
  routeHeader.routeName = data.routeName ?? "";
  routeHeader.prdId = data.prdId ?? "";
  routeHeader.description = data.description ?? "";
  routeHeader.useYn = data.useYn ?? "Y";

  headerChanged = false;
  stepChanged = false;

  initializing = false;
}

// 라우트 ID 입력 이벤트
document.getElementById("modalRouteId").addEventListener("input", (e) => {
	if (initializing) return;
	
	headerChanged = true;
	
	routeHeader.routeId = e.target.value;
});

// 라우트 이름 변경 이벤트
document.getElementById("modalRouteName").addEventListener("input", (e) => {
	if (initializing) return;
	
	headerChanged = true;
	
	routeHeader.routeName = e.target.value;
});

// 라우트 설명 변경 이벤트
document.getElementById("modalRouteRemark").addEventListener("input", (e) => {
	if (initializing) return;
	
	headerChanged = true;
	
	routeHeader.remark = e.target.value;
});

// 라우트 활성 변경 이벤트
document.getElementById("modalRouteUseYn").addEventListener("change", (e) => {
	if (initializing) return;
	
	headerChanged = true;
	
	routeHeader.useYn = e.target.value;
});

// 저장버튼 클릭 이벤트 
document.getElementById("saveRouteBtn").addEventListener("click", async () => {
	routeStepGrid.finishEditing();
	
	if (!validateBeforeSave()) {
		return;
	}
	
	if (!headerChanged && !stepChanged) {
		alert("변경된 내역이 없습니다.");
		return;
	}
	
	if (isEdit) {
		if (headerChanged) {
			const data = collectRouteHeader();
			await saveHeader(data, routeHeader.routeId);
			headerChanged = false;
		}
		
		if (stepChanged) {
			const data = collectRouteStepData();
			await saveSteps(data, routeHeader.routeId);
			stepChanged = false;
		}
	} else {
		const data = collectCreateRouteData();
		await createHeader(data);
	}
	
	alert("저장했습니다.");
	
	await loadRoute("all");
	$('#route-modal').modal('hide');
});

function validateBeforeSave() {
	if (!validateHeader()) return false;
	if (!validateSteps()) return false;
	return true;
}

function validateHeader() {
	const routeName = document.getElementById("modalRouteName").value;
	
	if (!routeName?.trim()) {
		alert("라우트명을 입력하세요.");
		return false;
	}
	
	const routeId = document.getElementById("modalRouteId").value;
	
	if (!routeId?.trim()) {
		alert("라우트ID를 입력하세요.");
		return false;
	}
	
	const prdId = document.getElementById("prdCode").value;
	
	if (!prdId?.trim()) {
		alert("제품코드를 입력해주세요.");
		return false;
	}
	
	// 신규 라우트일 때만 검사
	if (!isEdit) {
		const inputId = `RT-${routeHeader.routeId}`;
		
		const duplicated = routeList?.some(
			r => r.routeId === inputId
		);

		if (duplicated) {
			alert("이미 존재하는 라우트ID입니다.");
			return false;
		}
	}
	return true;
}

function collectRouteHeader() {
	routeHeader.routeId = document.getElementById('modalRouteId').value;//라우트 ID
	routeHeader.prdId = document.getElementById('prdCode').value;
	routeHeader.routeName = document.getElementById("modalRouteName").value;
	routeHeader.useYn = document.getElementById('modalRouteUseYn').value;//사용여부
	routeHeader.description = document.getElementById('modalRouteRemark').value;//비고
	
	return {...routeHeader};
}

function collectCreateRouteData() {
	const modifiedData = routeStepGrid.getModifiedRows() || {};
	let createdRows = modifiedData.createdRows || [];
	
	routeHeader.prdId = document.getElementById('prdCode').value;
	routeHeader.routeId = document.getElementById('modalRouteId').value;
	
	const isEmptyRow = (row) => {
		return !row.stepSeq && !row.routeId && !row.useYn;
	}
	
	createdRows = createdRows.filter(row => !isEmptyRow(row));
	
	return {
		...routeHeader,
		items: createdRows
	}
}

async function createHeader(data) {
	try {
		const res = await fetch(apiUrl("/processMst/route/add"), {
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
		
	} catch (error) {
		console.error(error);
		alert(error.message || "저장에 실패했습니다.");
	}
}

async function saveHeader(data, routeId) {
	try {
		const res = await fetch(apiUrl(`/processMst/route/modify/${routeId}`), {
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
	} catch (error) {
		console.error(error);
		alert(error.message || "저장에 실패했습니다.");
	}
}

async function loadUnit(url) {
	
	try {
		const res = await fetch(apiUrl(url));
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


document.getElementById("routeRegistBtn").addEventListener("click", async () => {
	$('#route-modal').modal('show');
	
	isEdit = false;
	
	const modalTitle = document.getElementById("routeModalTitle");
	modalTitle.textContent = "라우트 등록";
	
	document.getElementById("searchProduct").disabled = false;
	
	resetModal();

	const modalElement = document.getElementById("route-modal");
		modalElement.addEventListener("shown.bs.modal", async function loadData() {
	      
	      	// 그리드 강제 리프레시
	      	routeStepGrid.refreshLayout();
			
			routeStepGrid.resetData([]);
	      
	      	// 이벤트 리스너 제거 (한 번만 실행되도록)
	      	modalElement.removeEventListener('shown.bs.modal', loadData);
	  }, { once: true }); // once 옵션으로 자동 제거
});

document.getElementById("addRouteStepRow").addEventListener("click", () => {
	routeStepGrid.prependRow();
});

// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async () => {
	await loadRoute(); // 라우트 조회
});