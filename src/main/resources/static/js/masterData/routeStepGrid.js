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

async function loadRouteStep(useYn, routeId) {
	try {
		const res = await fetch(`/processMst/routeStep?useYn=${useYn}&routeId=${routeId}`);
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패");
		}
		
		const data = await res.json();
		
		if (!data || data.length === 0) {
			routeStepGrid.resetData([]);
		}
		
		routeStepGrid.resetData(data);
	} catch (error) {
		console.error(error);
	}
}
