let itemList = [];

const itemGrid = new tui.Grid({
	el: document.getElementById("itemGrid"),
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
				header: "ITEMID",
				name: "itemId",
				hidden: true
			},
			{
				header: "품목코드",
				name: "itemCode",
				sortable: true,
				sortingType: 'asc',
			},
			{
				header: "품목명",
				name: "itemName",
				sortable: true,
				sortingType: 'asc',
			},
			{
				header: "품목타입",
				name: "itemType",
				formatter: ({value}) => {
					const type = matTypeList.find(item => item.value === value);
					return type ? type.text : value;
				}
			},
			{
				header: "단위",
				name: "itemUnit",
			},
		]
});

const selectItem = {};

itemGrid.on("click", ev => {
	if (ev.rowKey === null || ev.rowKey === undefined) return;
	
	const rowData = itemGrid.getRow(ev.rowKey);
	
	const isDuplicatedItem = (itemId, itemType) => {
		return safeStockList.some(
			stock => stock.itemId === itemId && stock.itemType === itemType
		);
	}
	
	if (isDuplicatedItem(rowData.itemId, rowData.itemType)) {
		alert("이미 안전재고로 등록된 품목입니다.");
		return;
	}
	
	selectItem.itemId = rowData.itemId;
	selectItem.itemCode = rowData.itemCode;
	selectItem.itemName = rowData.itemName;
	selectItem.itemType = rowData.itemType;
	selectItem.itemUnit = rowData.itemUnit;
	
	if (window.selectedBomRowKey !== undefined) {
		safeStockGrid.setValue(window.selectedBomRowKey, "itemId", selectItem.itemId);
		safeStockGrid.setValue(window.selectedBomRowKey, "itemCode", selectItem.itemCode);
		safeStockGrid.setValue(window.selectedBomRowKey, "itemName", selectItem.itemName);
		safeStockGrid.setValue(window.selectedBomRowKey, "itemType", selectItem.itemType);
		safeStockGrid.setValue(window.selectedBomRowKey, "itemUnit", selectItem.itemUnit);
	}
	
	itemModal.hide();
	
	// 선택된 rowKey 초기화
	window.selectedBomRowKey = undefined;
});

// 모달이 닫힐 때 이벤트
document.getElementById("item-modal").addEventListener("hidden.bs.modal", () => {
    // 검색어 초기화
    document.getElementById("itemKeyword").value = "";
    
    // 그리드를 전체 데이터로 복원
    if (itemList.length > 0) {
        itemGrid.resetData(itemList);
    }
});

let itemKeyword = "";

document.getElementById("itemKeyword").addEventListener("input", (e) => {
	itemKeyword = e.target.value;
});

document.getElementById("searchItemtBtn").addEventListener("click", () => {
	const keyword = itemKeyword.trim().toLowerCase();
	
	if (!keyword) {
		itemGrid.resetData([]);
		return;
	}
	
	const filterData = itemList.filter(item => {
		const itemName = item.itemName ? item.itemName.toLowerCase() : "";
		const itemCode = item.itemCode ? item.itemCode.toLowerCase() : "";
		
		return itemName.includes(keyword) || itemCode.includes(keyword);
	})
	
	itemGrid.resetData(filterData);
});

async function loaditems() {
	const ITEM_URL = "/masterData1/items";
	
	try {
		const res = await fetch (ITEM_URL);
		
		if (!res.ok) {
			throw new Error("데이터 로드 실패!");
		}	
		
		const data = await res.json();
		
		if (!data || data.length === 0) {
			itemGrid.resetData([]);
		}
		
		itemGrid.resetData(data);
		
		itemList = data;
	} catch (error) {
		console.error(error);
	}
}