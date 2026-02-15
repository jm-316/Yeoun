let processCodeList = [];
let processTypeList = [];

const routeStepCodeGrid = new tui.Grid({
	el: document.getElementById("routeStepCodeGrid"),
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
			sortable: true,
		},
		{
			header: "공정명",
			name: "processName",
			filter: "select",
		},
		{
			header: "공정유형",
			name: "processType",
			filter: "select",
		},
		{
			header: "설명",
			name: "description",
		},
		{
			header: "공정순서",
			name: "stepNo",
		},
	]
});

// 클릭 동작
routeStepCodeGrid.on('click', ev => {
	const rowData = routeStepCodeGrid.getRow(ev.rowKey);
	
	routeStepGrid.setValue(window.selectedBomRowKey, "processId", rowData.processId);
	routeStepGrid.setValue(window.selectedBomRowKey, "remark", rowData.processName);
	
	processCodeModal.hide();
	
	// 선택된 rowKey 초기화
	window.selectedBomRowKey = undefined;
});

// 공정코드 정보 불러오기
async function loadProcessCode(useYn) {
	try {
		const res = await fetch(apiUrl(`/processMst/processCodes?useYn=${useYn}`));
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패");
		}
		
		const data = await res.json();
		
		// 데이터가 없을 경우 빈배열 반환
		if (!data || data.length === 0) {
			routeStepCodeGrid.resetData([]);
		}
		
		routeStepCodeGrid.resetData(data);
		
	} catch (error) {
		console.error(error);
	}
}


// 페이지 로딩 시 실행하는 함수들
//window.addEventListener("DOMContentLoaded", async (e) => {
//	await loadprocessTypeCode();
//	await loadProcessCode("Y"); // 공정 코드 목록 조회
//	
//});
