// 결재선 관리 모달
let approverLineManageModal = null;
// 결재선 등록 모달
let approvalLineCreateModal = null;
// 템플릿용 조직도
let templateApproverTreeGrid = null;
let templateApproverOrgData = [];
let templateApproverList = [];

let templateApproverTreeData = null;


document.addEventListener('DOMContentLoaded', function () {
	// 결재선 관리 모달
    const modalEl = document.getElementById('approver-line-manage-modal');
	
    if (modalEl) {
        approverLineManageModal = new bootstrap.Modal(modalEl);
    }
	
	// 결재선 등록 모달
	const createModalEl = document.getElementById('approval-line-create-modal');
	
	if (createModalEl) {
	    approvalLineCreateModal = new bootstrap.Modal(createModalEl);
	}
	
	// 새결재선 버튼
	const addTemplateBtn = document.getElementById('add-approval-line-template-btn');
	
	if (addTemplateBtn) {
	    addTemplateBtn.addEventListener('click', openApprovalLineCreateModal);
	}
	
	// 템플릿 저장 버튼
	const saveTemplateBtn = document.getElementById('save-approval-line-template-btn');
	
	if (saveTemplateBtn) {
	    saveTemplateBtn.addEventListener('click', saveApprovalLineTemplate);
	}

	// 템플릿용 조직도에서 "선택한 직원 추가" 버튼
	const addTemplateApproversBtn = document.getElementById('add-template-approvers-btn');
	
	if (addTemplateApproversBtn) {
	    addTemplateApproversBtn.addEventListener('click', addSelectedTemplateApprovers);
	}
});

// 결재선 관리 모달 열기 함수
function openApproverLineManageModal() {
    if (!approverLineManageModal) {
        return;
    }

    approverLineManageModal.show();
}

// 새 결재선 등록 모달 함수
async function openApprovalLineCreateModal() {
    if (!approvalLineCreateModal) {
        return;
    }

    // 초기화
    document.getElementById('approval-line-name').value = '';
    templateApproverList = [];
    renderTemplateApproverList();

    // 조직도 데이터 로드 + 그리드 렌더링
	await getApproverOrganizationChartForTemplate();
	await renderApproverOrgGridForTemplate();

    approvalLineCreateModal.show();
}

// 조직도 데이터 로드
async function getApproverOrganizationChartForTemplate() {
    try {
        const response = await fetch(apiUrl('api/schedules/organizationChart'), {
            method: 'GET'
        });

        if (!response.ok) {
            throw new Error('조직도 데이터 로드 실패');
        }

        const data = await response.json();
        console.log('1. 데이터 로드 완료:', data.data);

        buildApproverTreeForTemplate(data.data);
        console.log('2. 트리 변환 완료:', templateApproverTreeData);

    } catch (error) {
        console.error('조직도 데이터 로드 에러:', error);
        alert('조직도 데이터를 불러올 수 없습니다.');
    }
}

// 조직도 데이터를 트리 구조로 변환 (approvalModal.js의 buildApproverTree 참고)
async function buildApproverTreeForTemplate(flatList) {
    const deptMap = {};

    flatList.forEach((item) => {
	    if (!deptMap[item.DEPT_ID]) {
	        deptMap[item.DEPT_ID] = {
	            name: item.DEPT_NAME,
	            deptId: item.DEPT_ID,
	            type: 'department',
	            parentId: item.PARENT_ID ?? null,
	            children: []
	        };
	    }

	    if (item.EMP_ID) {
	        deptMap[item.DEPT_ID].children.push({
	            name: item.EMP_NAME,
	            empId: item.EMP_ID,
	            posName: item.POS_NAME ?? '-',
	            type: 'employee'
	        });
	    }
	});

	console.log("deptMap : ", deptMap);
	console.log("deptMap.keys: ", Object.keys(deptMap));  // ← 이 줄 추가
	
    const treeRoot = [];
	
    Object.values(deptMap).forEach((dept) => {
		console.log('dept:', dept.name, '/ parentId:', dept.parentId, '/ has in deptMap:', !!deptMap[dept.parentId]);

		
        if (!dept.parentId || !deptMap[dept.parentId]) {
            treeRoot.push(dept);
        } else {
            deptMap[dept.parentId].children.push(dept);
        }
    });

	console.log("treeRoot : ", treeRoot);  // ← 이 줄도 추가
    templateApproverTreeData = convertApproverTreeNodesForTemplate(treeRoot);
}

// 트리 노드를 Toast UI Grid 형식으로 변환 (approvalModal.js의 convertApproverTreeNodes 참고)
function convertApproverTreeNodesForTemplate(nodes) {
    return nodes.map((node) => {
        const newNode = { ...node };

		if (Array.isArray(newNode.children) && newNode.children.length > 0) {
            newNode._children = convertApproverTreeNodesForTemplate(newNode.children);
        }
        delete newNode.children;

        newNode._attributes = { expanded: true };
        return newNode;
    });
}

// 조직도 그리드 렌더링 (approvalModal.js의 renderApproverOrgGrid 참고)
async function renderApproverOrgGridForTemplate() {
	console.log('renderApproverOrgGridForTemplate 시작');
	console.log('templateApproverTreeData :', templateApproverTreeData);
	
    tui.Grid.setLanguage('ko', {
        display: {
            noData: '데이터가 없습니다.',
            loadingData: '데이터를 불러오는 중입니다.'
        }
    });

    if (templateApproverTreeGrid) {
        templateApproverTreeGrid.destroy();
		templateApproverTreeGrid = null;
    }
	
	try {
	    templateApproverTreeGrid = new tui.Grid({
	        el: document.getElementById('template-approver-org-grid'),
	        data: templateApproverTreeData,
	        rowHeaders: ['checkbox'],
	        bodyHeight: 350,
	        filter: true,
	        treeColumnOptions: {
	            name: 'name',
	            useCascadingCheckbox: true
	        },
	        columns: [
	            {
	                header: '이름',
	                name: 'name',
					width: 300,
	                treeColumn: true,
	                align: 'left',
	                filter: 'text',
					formatter: function({row}) {
					    if (row.type === 'employee') {
					        return `${row.name} (${row.empId}) - ${row.posName}`;
					    } else {
					        return `📁 ${row.name}`;
					    }
					}
	            }
	        ]
	    });
	
	    // ← 이 부분 추가
	    setTimeout(() => {
	        if (templateApproverTreeGrid) {
	            templateApproverTreeGrid.refreshLayout();
	            console.log('✓ 그리드 레이아웃 새로고침');
	        }
	    }, 200);

	} catch (error) {
	    console.error('그리드 생성 중 에러:', error);
	}
}

// 템플릿용 조직도에서 선택한 직원 추가
function addSelectedTemplateApprovers() {
    if (!templateApproverTreeGrid) {
        return;
    }

    const checkedRows = templateApproverTreeGrid.getCheckedRows();
    const selectedEmployees = checkedRows.filter((row) => row.type === 'employee');

    if (selectedEmployees.length === 0) {
        alert('선택한 직원이 없습니다.');
        return;
    }

    const remainingSlots = 3 - templateApproverList.length;
    if (selectedEmployees.length > remainingSlots) {
        alert(`최대 3명까지만 추가할 수 있습니다. (남은 슬롯: ${remainingSlots})`);
        return;
    }

    let addedCount = 0;
    selectedEmployees.forEach((emp) => {
        if (!templateApproverList.find((item) => item.empId === emp.empId)) {
            templateApproverList.push({
                empId: emp.empId,
                empName: emp.name,
                posName: emp.posName,
                stepOrder: templateApproverList.length + 1
            });
            addedCount++;
        }
    });

    renderTemplateApproverList();

    if (addedCount > 0) {
        alert(`${addedCount}명의 직원이 추가되었습니다.`);
    } else {
        alert('이미 추가된 직원입니다.');
    }
}

// 템플릿용 결재자 목록 렌더링
function renderTemplateApproverList() {
    const tbody = document.getElementById('template-approver-list-body');
    const countEl = document.getElementById('template-approver-count');
    tbody.innerHTML = '';

	templateApproverList.forEach((item, index) => {
	    const tr = document.createElement('tr');
	    tr.innerHTML = `
	        <td>${index + 1}차</td>
	        <td>${item.posName || '-'}</td>
	        <td>${item.empName} (${item.empId})</td>
	        <td>
				<button type="button"
				        class="btn btn-sm btn-outline-secondary move-up-template-btn"
				        data-index="${index}"
				        style="padding: 2px 6px; font-size: 12px;"
				        ${index === 0 ? 'disabled' : ''}>
				    <i class="bi bi-arrow-up"></i>
				</button>
				<button type="button"
				        class="btn btn-sm btn-outline-secondary move-down-template-btn"
				        data-index="${index}"
				        style="padding: 2px 6px; font-size: 12px;"
				        ${index === templateApproverList.length - 1 ? 'disabled' : ''}>
				    <i class="bi bi-arrow-down"></i>
				</button>
				<button type="button"
				        class="btn btn-sm btn-danger delete-template-approver-btn"
				        data-index="${index}"
				        style="padding: 2px 6px; font-size: 12px;">
				    <i class="bi bi-x"></i>
				</button>
	        </td>
	    `;
	    tbody.appendChild(tr);
	});

	// 위로 이동
	tbody.querySelectorAll('.move-up-template-btn').forEach((btn) => {
	    btn.addEventListener('click', function () {
	        const idx = parseInt(this.dataset.index);
	        if (idx > 0) {
	            [templateApproverList[idx], templateApproverList[idx - 1]] = 
	            [templateApproverList[idx - 1], templateApproverList[idx]];
	            renderTemplateApproverList();
	        }
	    });
	});

	// 아래로 이동
	tbody.querySelectorAll('.move-down-template-btn').forEach((btn) => {
	    btn.addEventListener('click', function () {
	        const idx = parseInt(this.dataset.index);
	        if (idx < templateApproverList.length - 1) {
	            [templateApproverList[idx], templateApproverList[idx + 1]] = 
	            [templateApproverList[idx + 1], templateApproverList[idx]];
	            renderTemplateApproverList();
	        }
	    });
	});

	// 삭제
	tbody.querySelectorAll('.delete-template-approver-btn').forEach((btn) => {
	    btn.addEventListener('click', function () {
	        const idx = parseInt(this.dataset.index);
	        templateApproverList.splice(idx, 1);
	        renderTemplateApproverList();
	    });
	});

	if (countEl) {
	    countEl.textContent = `${templateApproverList.length} / 3`;
	}
}

// 템플릿 저장
function saveApprovalLineTemplate() {
    const templateName = document.getElementById('approval-line-name').value.trim();

    if (!templateName) {
        alert('결재선 이름을 입력하세요.');
        return;
    }

    if (templateApproverList.length === 0) {
        alert('최소 1명 이상의 결재자를 선택하세요.');
        return;
    }

    // TODO: 나중에 서버에 POST /new/approval/line-template로 저장
    console.log('저장할 템플릿:', {
        templateName: templateName,
        approvers: templateApproverList
    });

    alert(`결재선 "${templateName}"이 저장되었습니다.`);
    approvalLineCreateModal.hide();
}
