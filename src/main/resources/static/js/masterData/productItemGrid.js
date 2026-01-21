let productItemData = [];
let prdTypeList = []; // 완제품 타입 리스트 (드롭다운에 사용)
let prdUnitList = []; // 완제품 단위 리스트 (드롭다운에 사용)
let prdTypeMap = {};

const productItemGrid = new tui.Grid({
	el: document.getElementById("productsGrid"),
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
			header: "PRDID",
			name: "prdId",
			hidden: true
		},
		{
			header: "완제품코드",
			name: "prdCode",
		},
		{
			header: "완제품명",
			name: "prdName",
		},
		{
			header: "타입",
			name: "prdType",
			formatter: ({value}) => {
				const type = prdTypeList.find(item => item.value === value);
				return type ? type.text : value;
			}
		},
		{
			header: "단위",
			name: "prdUnit",
		},
		{
			header: "유효일자(개월)",
			name: "effectiveDate",
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

// 클릭 동작
productItemGrid.on('click', ev => {
	const rowData = productItemGrid.getRow(ev.rowKey);
	
	document.getElementById("prdId").value = rowData.prdId;
	document.getElementById("productName").value = rowData.prdName;
	
	// 모달 닫기
	productItemModal.hide();
});

// 모달이 닫힐 때 이벤트
document.getElementById("products-modal").addEventListener("hidden.bs.modal", () => {
    // 검색어 초기화
    document.getElementById("productKeyword").value = "";
    
    // 그리드를 전체 데이터로 복원
    if (productItemData.length > 0) {
        productItemGrid.resetData(productItemData);
    }
});

let searchProductKeyword = "";

document.getElementById("productKeyword").addEventListener("input", (e) => {
	searchProductKeyword = e.target.value;
});

document.getElementById("searchProductBtn").addEventListener("click", (e) => {
	const keyword = searchProductKeyword.trim().toLowerCase();
	
	// 검색어가 없으면 빈 화면 보여주기
	if (!keyword) {
		productItemGrid.resetData([]);
		return;
	}
	const filterData = productItemData.filter(item => {
		const prdName = item.prdName ? item.prdName.toLowerCase() : "";
		const prdCode = item.prdCode ? item.prdCode.toLowerCase() : "";
		
		 return prdName.includes(keyword) || prdCode.includes(keyword);
	});
	
	productItemGrid.resetData(filterData);
});


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
		
	} catch (e) {
		console.error(e);
	}
}

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
			productItemGrid.resetData([]);
		}

		productItemGrid.resetData(data);
		productItemData = data;
		
	} catch (error) {
		console.error(error);
	}
}

// 공통코드 가져올 URL
const PRODUCT_UNIT_URL = "/commomCode/unit";

// 페이지 로딩 시 실행하는 함수들
window.addEventListener("DOMContentLoaded", async (e) => {
	await loadPrdTypeCode(); // 공통코드에서 원재료 타입 조회
	await loadUnit(PRODUCT_UNIT_URL); // 공통코드에서 단위 조회

	//스피너  off
//	hideSpinner();
});

