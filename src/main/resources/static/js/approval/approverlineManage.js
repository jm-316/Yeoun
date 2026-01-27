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
	
	// 결재선 적용버튼
	const applyBtn = document.getElementById('apply-approval-line-template-btn');
	
	if (applyBtn) {
	    applyBtn.addEventListener('click', applyApprovalLineTemplate);
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
	
	// 기본값 설정 버튼
	const setDefaultBtn = document.getElementById('set-default-approval-line-btn');
	
	if (setDefaultBtn) {
	    setDefaultBtn.addEventListener('click', setDefaultApprovalLineTemplate);
	}

	// 삭제 버튼
	const deleteBtn = document.getElementById('delete-approval-line-template-btn');
	
	if (deleteBtn) {
	    deleteBtn.addEventListener('click', deleteApprovalLineTemplate);
	}
});

// 결재선 관리 모달 열기 함수
function openApproverLineManageModal() {
    if (!approverLineManageModal) {
        return;
    }
	// 결재선 목록 조회
	loadMyApprovalLineTemplates();
    approverLineManageModal.show();
}

// 결재선 목록 조회 함수
function loadMyApprovalLineTemplates() {

	// 목록 초기화
	document.getElementById('approval-line-template-list').innerHTML = '';
	
	// 선택 상태 초기화
	document.querySelectorAll('#approval-line-template-list li').forEach(li => {
	    li.classList.remove('active');
	    li.style.backgroundColor = '';
		currentSelectedTemplate = null;
	});
	
	// 상세 정보 초기화
	document.getElementById('approval-line-template-detail-body').innerHTML = 
	    '<tr><td colspan="3" class="text-center text-muted">템플릿을 선택하세요.</td></tr>';
		
	// 버튼 비활성화
	document.getElementById('set-default-approval-line-btn').disabled = true;
	document.getElementById('delete-approval-line-template-btn').disabled = true;
	document.getElementById('apply-approval-line-template-btn').disabled = true;
	
    fetch(apiUrl('/api/manage/approval/line-template/my'), {
        method: 'GET',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('목록 조회 실패');
        }
        return response.json();
    })
    .then(result => {
        console.log('결재선 템플릿 목록:', result);
        renderApprovalLineTemplateList(result);
    })
    .catch(error => {
        console.error('목록 조회 중 에러:', error);
        alert('결재선 목록을 불러올 수 없습니다.');
    });
}

// 결재선 템플릿 목록 그리기
function renderApprovalLineTemplateList(templates) {
    const listEl = document.getElementById('approval-line-template-list');
    listEl.innerHTML = '';
    
    if (!templates || templates.length === 0) {
        listEl.innerHTML = '<li class="list-group-item text-muted">저장된 결재선이 없습니다.</li>';
        return;
    }
    
    templates.forEach((template, index) => {
        const li = document.createElement('li');
        li.className = 'list-group-item cursor-pointer';
        li.style.cursor = 'pointer';
        
        const defaultBadge = template.isDefault === 'Y' 
            ? '<span class="badge bg-primary ms-2">기본값</span>' 
            : '';
        
        li.innerHTML = `
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <strong>${template.templateName}</strong>
                    ${defaultBadge}
                    <small class="text-muted d-block">
                        ${template.details.length}명 
                    </small>
                </div>
            </div>
        `;
        
        // 템플릿 선택 시
        li.addEventListener('click', function() {
            selectApprovalLineTemplate(template, li);
        });
        
        listEl.appendChild(li);
    });
}

// 현재 선택된 템플릿 전역변수
let currentSelectedTemplate = null;

// 템플릿 선택
function selectApprovalLineTemplate(template, liElement) {
    // 기존 선택 해제
    document.querySelectorAll('#approval-line-template-list li').forEach(li => {
        li.classList.remove('active');
        li.style.backgroundColor = '';
    });
    
    // 현재 선택 표시
    liElement.classList.add('active');
    liElement.style.backgroundColor = '#e7f3ff';
	
	// 현재 선택 템플릿 저장
	currentSelectedTemplate = template;
	
	// 버튼 활성화
	document.getElementById('set-default-approval-line-btn').disabled = false;
	document.getElementById('delete-approval-line-template-btn').disabled = false;
	document.getElementById('apply-approval-line-template-btn').disabled = false;

	// 오른쪽 상세 정보 표시
    renderApprovalLineTemplateDetail(template);
}

// 템플릿 상세 정보 렌더링
function renderApprovalLineTemplateDetail(template) {
    const tbody = document.getElementById('approval-line-template-detail-body');
    tbody.innerHTML = '';
    
    if (!template.details || template.details.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">결재자 정보가 없습니다.</td></tr>';
        return;
    }
    
    template.details.forEach((detail) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${detail.stepOrder}차</td>
            <td>${detail.posName || '-'}</td>
            <td>${detail.approverName} (${detail.approverId})</td>
        `;
        tbody.appendChild(tr);
    });
}

// 결재선 적용
function applyApprovalLineTemplate() {
    if (!currentSelectedTemplate) {
        alert('적용할 결재선을 선택하세요.');
        return;
    }

    if (!currentSelectedTemplate.details || currentSelectedTemplate.details.length === 0) {
        alert('결재자가 없는 결재선입니다.');
        return;
    }

    // 선택된 템플릿의 결재자들을 approvalModal.js의 approverList로 변환
    const selectedApprovers = currentSelectedTemplate.details.map(detail => ({
        empId: detail.approverId,
        empName: detail.approverName,
        posName: detail.posName,
//        stepOrder: detail.stepOrder
    }));

    // approvalModal.js의 전역 변수에 세팅
    approverList = selectedApprovers;

    console.log('결재선 적용:', approverList);

    // 결재문서 모달의 결재선 테이블 업데이트
    renderApproverList(false);

    alert(`"${currentSelectedTemplate.templateName}" 결재선이 적용되었습니다.`);

    // 결재선 관리 모달 닫기
    approverLineManageModal.hide();
}

// ---------------------------------------------------------------------------
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

// 조직도 데이터를 트리 구조로 변환
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

    const treeRoot = [];
	
    Object.values(deptMap).forEach((dept) => {
		console.log('dept:', dept.name, '/ parentId:', dept.parentId, '/ has in deptMap:', !!deptMap[dept.parentId]);

		
        if (!dept.parentId || !deptMap[dept.parentId]) {
            treeRoot.push(dept);
        } else {
            deptMap[dept.parentId].children.push(dept);
        }
    });

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

// 조직도 그리드 그리기
async function renderApproverOrgGridForTemplate() {
	
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

	// 요청 데이터 구성
	const payload = {
	    templateName: templateName,
	    approvers: templateApproverList.map((approver, index) => ({
	        empId: approver.empId,
	        stepOrder: index + 1
	    }))
	};
	
//	console.log('저장 요청:', payload);
	
	// API 호출
	fetch(apiUrl('/api/manage/approval/line-template'), {
	    method: 'POST',
	    headers: {
	        [csrfHeader]: csrfToken,
	        'Content-Type': 'application/json'
	    },
	    body: JSON.stringify(payload)
	})
	.then(response => {
//	    console.log('응답 상태:', response.status);
	    if (!response.ok) {
	        throw new Error('저장 실패');
	    }
	    return response.json();
	})
	.then(result => {
//	    console.log('저장 성공:', result);
	    alert(`결재선 "${templateName}"이 저장되었습니다.`);
	    approvalLineCreateModal.hide();
		loadMyApprovalLineTemplates();
	})
	.catch(error => {
	    console.error('저장 중 에러:', error);
	    alert('결재선 저장 중 오류가 발생했습니다.');
	});
}

// ================================================================
// 기본값 설정
function setDefaultApprovalLineTemplate() {
    if (!currentSelectedTemplate) {
        alert('기본값으로 설정할 결재선을 선택하세요.');
        return;
    }

    if (!confirm(`"${currentSelectedTemplate.templateName}"을(를) 기본값으로 설정하시겠습니까?`)) {
        return;
    }

    fetch(apiUrl(`/api/manage/approval/line-template/${currentSelectedTemplate.templateId}/default`), {
        method: 'PATCH',
        headers: {
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('기본값 설정 실패');
        }
        return response.text();
    })
    .then(result => {
        console.log('기본값 설정 성공');
        alert('기본값이 설정되었습니다.');
        loadMyApprovalLineTemplates();  // 목록 새로고침
    })
    .catch(error => {
        console.error('기본값 설정 중 에러:', error);
        alert('기본값 설정 중 오류가 발생했습니다.');
    });
}

// ---------------------------------------------------------------------

// 삭제
function deleteApprovalLineTemplate() {
    if (!currentSelectedTemplate) {
        alert('삭제할 결재선을 선택하세요.');
        return;
    }

    if (!confirm(`"${currentSelectedTemplate.templateName}"을(를) 삭제하시겠습니까?\n(삭제 후 복구할 수 없습니다.)`)) {
        return;
    }

    fetch(apiUrl(`/api/manage/approval/line-template/${currentSelectedTemplate.templateId}`), {
        method: 'DELETE',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('삭제 실패');
        }
        return response.text();
    })
    .then(result => {
        console.log('결재선 삭제 성공');
        alert('결재선이 삭제되었습니다.');
        loadMyApprovalLineTemplates();  // 목록 새로고침
    })
    .catch(error => {
        console.error('삭제 중 에러:', error);
        alert('삭제 중 오류가 발생했습니다.');
    });
}

