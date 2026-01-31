let routeList = [];
let isEdit = false;

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
		const res = await fetch("/processMst/routes");
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패");
		}
		
		const data = await res.json();
		
		if (!data || data.length === 0) {
			routeGrid.resetData([]);
		}
		
		routeGrid.resetData(data);
		
		console.log(data);
		
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
		
		// 예: 모달 열기, 상세 정보 표시 등		
		$('#route-modal').modal('show');
		
		window.selectedBomRowKey = rowKey;
		
		const modalElement = document.getElementById("route-modal");
			modalElement.addEventListener("shown.bs.modal", async function loadData() {
		    	await loadRouteStep("all", routeId);
		      
		      	// 그리드 강제 리프레시
		      	routeStepGrid.refreshLayout();
		      
		      	// 이벤트 리스너 제거 (한 번만 실행되도록)
		      	modalElement.removeEventListener('shown.bs.modal', loadData);
		  }, { once: true }); // once 옵션으로 자동 제거
		
		
		isEdit = true;
		
		fillModal(route);
		
		await loadRoute();
	}
});

// route 상세 조회
async function loadRouteDetail(routeId) {
	try {
		const res = await fetch(`/processMst/route/${routeId}`);
		
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
	document.getElementById('modalProcessprdId').value = data.prdId;//제품코드
	document.getElementById('modalRouteName').value = data.routeName;//라우트명
	document.getElementById('modalRouteUseYn').value = data.useYn;//사용여부
	document.getElementById('modalRouteRemark').value = data.description;//비고
	
	document.getElementById('modalRouteId').readOnly = true;
}

function resetModal() {
	document.getElementById('modalRouteId').value = '';//라우트 ID
	document.getElementById('modalProcessprdId').value = '';//제품코드
	document.getElementById('modalRouteName').value = '';//라우트명
	document.getElementById('modalRouteUseYn').value = 'Y';//사용여부
	document.getElementById('modalRouteRemark').value = '';//비고
	
	document.getElementById('modalRouteId').readOnly = false;
	
	isEdit = false;
}

// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async () => {
	await loadRoute(); // 라우트 조회
});