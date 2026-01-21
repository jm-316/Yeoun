let materialItemData = [];
let matTypeList = []; // 원재료 타입 리스트 (드롭다운에 사용)
let unitList = []; // 원재료 단위 리스트 (드롭다운에 사용)
let matTypeMap = {};

const matItemsModal = new bootstrap.Modal(document.getElementById("matItems-modal"));

const materialItemGrid = new tui.Grid({
	el: document.getElementById("matItemsGrid"),
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

const selectMaterial = {}

// 클릭 동작
materialItemGrid.on('click', ev => {
	const rowData = materialItemGrid.getRow(ev.rowKey);
	
	selectMaterial.matId = rowData.matId;
	selectMaterial.matCode = rowData.matCode;
	selectMaterial.matName = rowData.matName;
	selectMaterial.matUnit = rowData.matUnit;
	selectMaterial.matType = rowData.matType;
	selectMaterial.effectiveDate = rowData.effectiveDate;
	
	// BOMItemGird에 선택한 원재료 정보 적용
	if (window.selectedBomRowKey !== undefined && targetGrid === "bomItem") {
		bomItemGrid.setValue(window.selectedBomRowKey, 'matId', selectMaterial.matId);
        bomItemGrid.setValue(window.selectedBomRowKey, 'matCode', selectMaterial.matCode);
        bomItemGrid.setValue(window.selectedBomRowKey, 'matName', selectMaterial.matName);
        bomItemGrid.setValue(window.selectedBomRowKey, 'bomUnit', selectMaterial.matUnit);
	} else if (window.selectedBomRowKey !== undefined && targetGrid === "registBom") {
		registBomItemGrid.setValue(window.selectedBomRowKey, 'matId', selectMaterial.matId);
		registBomItemGrid.setValue(window.selectedBomRowKey, 'matCode', selectMaterial.matCode);
		registBomItemGrid.setValue(window.selectedBomRowKey, 'matName', selectMaterial.matName);
		registBomItemGrid.setValue(window.selectedBomRowKey, 'matType', selectMaterial.matType);
		registBomItemGrid.setValue(window.selectedBomRowKey, 'matUnit', selectMaterial.matUnit);
		registBomItemGrid.setValue(window.selectedBomRowKey, 'effectiveDate', selectMaterial.effectiveDate);
	}
	
	// 모달 닫기
	matItemsModal.hide();
	
	// 선택된 rowKey 초기화
	window.selectedBomRowKey = undefined;
});

// 모달이 닫힐 때 이벤트
document.getElementById("matItems-modal").addEventListener("hidden.bs.modal", () => {
    // 검색어 초기화
    document.getElementById("materialKeyword").value = "";
    
    // 그리드를 전체 데이터로 복원
    if (materialItemData.length > 0) {
        materialItemGrid.resetData(materialItemData);
    }
});

let searchKeyword = "";

document.getElementById("materialKeyword").addEventListener("input", (e) => {
	searchKeyword = e.target.value;
});

document.getElementById("searchbtn").addEventListener("click", (e) => {
	const keyword = searchKeyword.trim().toLowerCase();
	
	// 검색어가 없으면 빈 화면 보여주기
	if (!keyword) {
		materialItemGrid.resetData([]);
		return;
	}
	const filterData = materialItemData.filter(item => {
		const matName = item.matName ? item.matName.toLowerCase() : "";
		const matCode = item.matCode ? item.matCode.toLowerCase() : "";
		
		 return matName.includes(keyword) || matCode.includes(keyword);
	});
	
	materialItemGrid.resetData(filterData);
});


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
		
	} catch (e) {
		console.error(e);
	}
}

// 공통코드에서 단위 가져오기
const MATERIAL_UNIT_URL = "/commomCode/unit";

// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async (e) => {
	await loadMatTypeCode(); // 공통코드에서 원재료 타입 조회
	await loadUnit(MATERIAL_UNIT_URL); // 공통코드에서 단위 조회

	//스피너  off
//	hideSpinner();
});
//
//function showSpinner() {
//	document.getElementById('loading-overlay').style.display = 'flex';
//}
//function hideSpinner() {
//	document.getElementById('loading-overlay').style.display = 'none';
//}
