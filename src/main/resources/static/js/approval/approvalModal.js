// ============================================
// 전역 변수
// ============================================


let selectedFiles = [];  // 선택된 파일 배열
const MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10MB
const MAX_FILE_COUNT_APPROVAL = 5;

// 현재 결재 문서
let CURRENT_APPROVAL_ID = null;

// 결재자 목록 배열
let approverList = [];

// 조직도 관련
let approverTreeGrid = null;
let approverTreeData = null;

// 최대 결재자 수
const MAX_APPROVERS = 3;
// ============================================


// ============================================
// 결재문서 모달
// ============================================

const approvalModalEl = document.getElementById('approval-modal');
const approvalModal   = new bootstrap.Modal(approvalModalEl);

// 결재모달 열기 함수
async function openApprovalModal(mode, options = {}) {
    // 1) 항상 먼저 초기화
    resetApprovalForm();

    const titleEl   = document.getElementById('approval-title');
    const reasonEl  = document.getElementById('reason-write');
	const formTypeSelect = document.getElementById('form-type-select');
	const finishDateEl = document.getElementById('finish-date');

	// 버튼 요소들
	const submitBtn = document.getElementById('submit-btn');
	const approveBtn = document.getElementById('approve-btn');
	const finalApproveBtn = document.getElementById('final-approve-btn');
	const rejectBtn = document.getElementById('reject-btn');
	
	// 모달 제목
	const modalTitle = document.querySelector('#approval-modal .modal-title');

    if (mode === 'create') {
		// 기안 모드
		modalTitle.textContent = '결재 문서 작성';

		// 등록 버튼만 표시
		submitBtn.style.display = '';
		approveBtn.style.display = 'none';
		finalApproveBtn.style.display = 'none';
		rejectBtn.style.display = 'none';

		// 입력 필드 활성화
		if (titleEl)  titleEl.readOnly = false;
		if (reasonEl) reasonEl.readOnly = false;

		// 결재선 추가 버튼 활성화
		document.getElementById('open-approver-org-btn').style.display = '';		
    }

    if (mode === 'view') {
		// 조회 모드
		modalTitle.textContent = '결재 문서 조회';

		// 등록 버튼 숨김
		submitBtn.style.display = 'none';

		// ========== 모든 입력 필드 읽기 전용 ==========
		if (titleEl) titleEl.readOnly = true;
		if (reasonEl) reasonEl.readOnly = true;
		if (formTypeSelect) formTypeSelect.disabled = true;
		if (finishDateEl) finishDateEl.readOnly = true;

		// 휴가 필드 비활성화
		const leaveType = document.getElementById('leave-type');
		const leaveStartDate = document.getElementById('leave-start-date');
		const leaveEndDate = document.getElementById('leave-end-date');
		if (leaveType) leaveType.disabled = true;
		if (leaveStartDate) leaveStartDate.readOnly = true;
		if (leaveEndDate) leaveEndDate.readOnly = true;

		// 지출 필드 비활성화
		const expenseType = document.getElementById('expense-type');
		const expenseDate = document.getElementById('expense-date');
		const expenseVendor = document.getElementById('expense-vendor');
		const expenseAmount = document.getElementById('expense-amount');
		if (expenseType) expenseType.disabled = true;
		if (expenseDate) expenseDate.readOnly = true;
		if (expenseVendor) expenseVendor.readOnly = true;
		if (expenseAmount) expenseAmount.readOnly = true;

		// 결재선 추가 버튼 숨김
		document.getElementById('open-approver-org-btn').style.display = 'none';

		// 첨부파일 업로드 버튼 숨김
		document.getElementById('file-upload-btn').style.display = 'none';

		// 일단 결재 버튼 전부 숨김 (데이터 로드 후 권한 확인)
		approveBtn.style.display = 'none';
		finalApproveBtn.style.display = 'none';
		rejectBtn.style.display = 'none';

		// 서버에서 문서 데이터 로드
		await loadApprovalDetail(options.approvalId);
    }

    approvalModal.show();  // 실제 모달 열기[web:40][web:55]
}


// 결재문서 모달 초기화
function resetApprovalForm() {
	CURRENT_APPROVAL_ID = null;
	
    const formEl          = document.querySelector('#approval-modal form');
	if (formEl) {
	    formEl.reset();
	}
	
    const titleEl         = document.getElementById('approval-title');
    const reasonEl        = document.getElementById('reason-write');
    const reasonCountEl   = document.getElementById('reason-char-count');
    const todayDateEl     = document.getElementById('today-date');
    const createDateEl    = document.getElementById('create-date');
    const finishDateEl    = document.getElementById('finish-date');

	document.getElementById('approval-creator').textContent = 
	    `${LOGIN_USER_NAME} (${LOGIN_USER_ID})`;
    // 브라우저 기본 form reset
    if (formEl) {
        formEl.reset();  // 모든 input/textarea 기본값으로 리셋
    }

    // 오늘 날짜 다시 세팅
    const today = new Date().toISOString().slice(0, 10);
    if (todayDateEl) {
        todayDateEl.textContent = today;
    }
    if (createDateEl) {
        createDateEl.value = today;
    }
    if (finishDateEl) {
		finishDateEl.readOnly = false;
        finishDateEl.value = '';
    }

    // 커스텀 초기화 (글자수, readOnly 등)
    if (reasonCountEl) {
        reasonCountEl.textContent = '0/3000자';
    }
    if (titleEl) {
        titleEl.readOnly = false;
        titleEl.value = '';
    }
    if (reasonEl) {
        reasonEl.readOnly = false;
        reasonEl.value = '';
    }
	
	// 버튼 초기화
	const submitBtn = document.getElementById('submit-btn');
	const approveBtn = document.getElementById('approve-btn');
	const finalApproveBtn = document.getElementById('final-approve-btn');
	const rejectBtn = document.getElementById('reject-btn');

	if (submitBtn) submitBtn.style.display = '';
	if (approveBtn) approveBtn.style.display = 'none';
	if (finalApproveBtn) finalApproveBtn.style.display = 'none';
	if (rejectBtn) rejectBtn.style.display = 'none';
	
	// 양식 타입 초기화
	const formTypeSelect = document.getElementById('form-type-select');
	if (formTypeSelect) {
	    formTypeSelect.value = 'free';
		formTypeSelect.disabled = false; 
	    toggleFormFields('free');
	}
	
	// 휴가 필드
	const leaveTypeEl      = document.getElementById('leave-type');
	const leaveStartDateEl = document.getElementById('leave-start-date');
	const leaveEndDateEl   = document.getElementById('leave-end-date');
	const leaveDaysEl      = document.getElementById('leave-days');

	if (leaveTypeEl)      leaveTypeEl.disabled = false;
	if (leaveStartDateEl) leaveStartDateEl.readOnly = false;
	if (leaveEndDateEl) {
	    leaveEndDateEl.readOnly  = false;
	    leaveEndDateEl.disabled  = false;   // 반차 로직에서 disabled 걸린 것까지 초기화
	    leaveEndDateEl.style.backgroundColor = ''; 
	}
	if (leaveDaysEl) {
	    leaveDaysEl.readOnly = true;        // 원래 자동계산이니까 readOnly 유지
	    leaveDaysEl.style.backgroundColor = '#e9ecef';
	}
	// 휴가 필드 초기화
	document.getElementById('leave-type').value = '';
	document.getElementById('leave-start-date').value = '';
	document.getElementById('leave-end-date').value = '';
	document.getElementById('leave-days').value = '';

	// 지출 필드
	const expenseTypeEl   = document.getElementById('expense-type');
	const expenseDateEl   = document.getElementById('expense-date');
	const expenseVendorEl = document.getElementById('expense-vendor');
	const expenseAmountEl = document.getElementById('expense-amount');

	if (expenseTypeEl)   expenseTypeEl.disabled = false;
	if (expenseDateEl)   expenseDateEl.readOnly = false;
	if (expenseVendorEl) expenseVendorEl.readOnly = false;
	if (expenseAmountEl) expenseAmountEl.readOnly = false;
	// 지출 필드 초기화
	document.getElementById('expense-type').value = '';
	document.getElementById('expense-date').value = '';
	document.getElementById('expense-vendor').value = '';
	document.getElementById('expense-amount').value = '';
	
	// 버튼들
	const fileUploadBtn  = document.getElementById('file-upload-btn');
	const approverOrgBtn = document.getElementById('open-approver-org-btn');

	if (fileUploadBtn)  fileUploadBtn.style.display = '';
	if (approverOrgBtn) approverOrgBtn.style.display = '';

    // 4) 모드별 버튼 상태 초기값 (기본: 기안 모드 기준)
    const saveBtn              = document.getElementById('saveBtn');
    const approvalCheckBtn     = document.getElementById('approvalCheckBtn');
    const approvalCompanionBtn = document.getElementById('approvalCompanionBtn');

    if (saveBtn) saveBtn.style.display = '';
    if (approvalCheckBtn) approvalCheckBtn.style.display = 'none';
    if (approvalCompanionBtn) approvalCompanionBtn.style.display = 'none';
	
	// 결재선 초기화
	approverList = [];
	renderApproverList(false);
	
	// 반려이유 숨김
	hideRejectReason();
	
	const approverSelect = document.getElementById('approver-select');
	if (approverSelect) {
	    approverSelect.selectedIndex = 0;
	}

	// 첨부파일 초기화
	selectedFiles = [];
	renderFileList();
	document.getElementById('file-input').value = '';
}

// 양식별 필드 표시/숨김
async function toggleFormFields(formType) {
	console.log("toggleFormFields");
    // 휴가 관련 행
    const leaveTypeRow = document.getElementById('leave-type-row');
    const leavePeriodRow = document.getElementById('leave-period-row');
    
    // 지출 관련 행
    const expenseTypeRow = document.getElementById('expense-type-row');
    const expenseDateRow = document.getElementById('expense-date-row');
    const expenseAmountRow = document.getElementById('expense-amount-row');
    
    // 전부 숨김
    leaveTypeRow.style.display = 'none';
    leavePeriodRow.style.display = 'none';
    expenseTypeRow.style.display = 'none';
    expenseDateRow.style.display = 'none';
    expenseAmountRow.style.display = 'none';
    
    // 양식 타입에 따라 표시
    if (formType === 'leave') {
        leaveTypeRow.style.display = '';
        leavePeriodRow.style.display = '';
    } else if (formType === 'expense') {
        expenseTypeRow.style.display = '';
        expenseDateRow.style.display = '';
        expenseAmountRow.style.display = '';
    }
}

// 휴가 일수 자동 계산
function calculateLeaveDays() {
    const startDate = document.getElementById('leave-start-date').value;
    const endDateInput = document.getElementById('leave-end-date');
    const endDate = endDateInput.value;
    const daysInput = document.getElementById('leave-days');
    const leaveType = document.getElementById('leave-type').value;
	
	// 반차면 일수 계산하지 않음
	if (leaveType !== '연차') {
	    return;
	}
	
    if (!startDate || !endDate) {
        daysInput.value = '';
        return;
    }
    
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    if (end < start) {
        alert('종료일이 시작일보다 빠릅니다.');
		endDateInput.value = '';
        daysInput.value = '';
        return;
    }
    
    // 일수 계산 (시작일과 종료일 포함)
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    
    daysInput.value = diffDays;
}

async function loadApprovalDetail(approvalId) {
    try {
		// 현재 여는 문서ID 저장
		CURRENT_APPROVAL_ID = approvalId;
		
        const response = await fetch(apiUrl(`new/approval/detail/${approvalId}`), {
            method: 'GET',
            headers: {
                [csrfHeader]: csrfToken
            }
        });
        
        if (!response.ok) throw new Error('조회 실패');
        
        const result = await response.json();
		const data = result.data;
        
        // 모달에 데이터 채우기
        await fillModalData(data);
        
        // ========== 결재 권한 확인 ==========
        const currentUserId = document.getElementById('currentUserId').value;
        const isApprover = checkApprovalPermission(data.approvers, currentUserId);
        
        const approveBtn = document.getElementById('approve-btn');
        const finalApproveBtn = document.getElementById('final-approve-btn');
        const rejectBtn = document.getElementById('reject-btn');
        
        if (isApprover) {
            approveBtn.style.display = '';
            finalApproveBtn.style.display = '';
            rejectBtn.style.display = '';
        } else {
            approveBtn.style.display = 'none';
            finalApproveBtn.style.display = 'none';
            rejectBtn.style.display = 'none';
        }
        
    } catch (error) {
        console.error('상세 조회 실패', error);
        alert('문서 조회에 실패했습니다.');
    }
}

// 결재 권한 확인 (내 차례인지)
function checkApprovalPermission(approvers, currentUserId) {
    if (!approvers || approvers.length === 0) return false;
    
    // 내가 결재자 목록에 있고, 내 차례(PENDING)이면 true
    const myApproval = approvers.find(a => a.approverId === currentUserId);
    
    if (!myApproval) return false;  // 결재자가 아님
    
    if (myApproval.status !== 'PENDING') return false;  // 이미 처리함
    
    // 내 앞 단계가 모두 승인되었는지 확인
    const previousApprovers = approvers.filter(a => a.stepOrder < myApproval.stepOrder);
    const allPreviousApproved = previousApprovers.every(a => a.status === 'APPROVED' || a.status === 'FINAL_APPROVED');
    
    return allPreviousApproved;  // 내 차례면 true
}

// 모달에 데이터 채우기
async function fillModalData(data) {
	console.log(data,"213213213");
    // 기본 정보
    document.getElementById('approval-title').value = data.document.approvalTitle;
    document.getElementById('reason-write').value = data.document.reason || '';
    document.getElementById('today-date').textContent = data.document.createdDate;
    document.getElementById('create-date').value = data.document.createdDate;
    document.getElementById('finish-date').value = data.document.finishDate;
    document.getElementById('form-type-select').value = data.document.formType;
    document.getElementById('approval-creator').textContent = `${data.document.empName}(${data.document.empId})`
    // 양식별 필드 토글 및 데이터 채우기
    await toggleFormFields(data.document.formType);
    
    // 휴가 정보
    if (data.leave) {
		console.log("leave");
        document.getElementById('leave-type').value = data.leave.leaveType;
        document.getElementById('leave-start-date').value = data.leave.leaveStartDate;
        document.getElementById('leave-end-date').value = data.leave.leaveEndDate;
        document.getElementById('leave-days').value = data.leave.leaveDays;
    }
    
    // 지출 정보
    if (data.expense) {
		console.log("expense");
        document.getElementById('expense-type').value = data.expense.expenseType;
        document.getElementById('expense-date').value = data.expense.expenseDate;
        document.getElementById('expense-vendor').value = data.expense.expenseVendor;
        document.getElementById('expense-amount').value = data.expense.expenseAmount;
    }
    
    // 결재선 정보
    if (data.approvers) {
        approverList = data.approvers.map(a => ({
            empId: a.approverId,
            empName: a.approverName,
            posName: a.posName,
			status: a.status,
			rejectReason: a.rejectReason
        }));
        await renderApproverList(true);
    }
    
	// 반려 사유 있을때 표시
	if (data.document.status === 'REJECTED' && data.document.rejectReason) {
	    showRejectReason(data.document.rejectReason);
	} else {
		hideRejectReason();
	}
	
    // 글자수 표시
    const reasonLength = (data.document.reason || '').length;
    document.getElementById('reason-char-count').textContent = `${reasonLength}/3000자`;

	// ========== 첨부파일 표시 (읽기 전용) ==========
	if (data.attachments && data.attachments.length > 0) {
	    renderAttachmentsReadOnly(data.attachments);
	} else {
	    // 첨부파일 없으면 빈 상태
	    document.getElementById('file-list').innerHTML = '';
	}	
}

// 반려사유 보이기
function showRejectReason(reason) {
    const container = document.getElementById('reject-reason-container');
    const textEl    = document.getElementById('reject-reason-text');
    if (!container || !textEl) return;

    textEl.textContent = reason;
    container.style.display = '';
}

// 반려사유 가리기
function hideRejectReason() {
    const container = document.getElementById('reject-reason-container');
    const textEl    = document.getElementById('reject-reason-text');
    if (!container || !textEl) return;

    textEl.textContent = '';
    container.style.display = 'none';
}

// 첨부파일 읽기 전용 표시 (다운로드 가능)
function renderAttachmentsReadOnly(fileData) {
    // 첨부파일 영역 초기화
    const fileListEl = document.getElementById('file-list');
    fileListEl.innerHTML = '';
    
    if (!fileData || fileData.length === 0) {
        return;
    }
    
    // 파일 개수만큼 반복
    fileData.forEach(function(file, index) {
        // DOM 요소 생성
        const fileItem = document.createElement('div');
        const iconEl = document.createElement('i');
        const nameSpan = document.createElement('span');
        const downloadEl = document.createElement('a');
        const downloadImg = document.createElement('img');
        
        // 클래스 및 속성 설정
        fileItem.classList.add('attach-file');
        iconEl.classList.add('fa-regular', 'fa-file', 'file-icon');
        nameSpan.textContent = file.originFileName + '  ';
        
        // 아이콘과 파일명 추가
        fileItem.appendChild(iconEl);
        fileItem.appendChild(nameSpan);
        
        // 다운로드 링크 설정
        downloadEl.href = `/files/download/${file.fileId}`;
        downloadEl.classList.add('file-download-link');
        downloadEl.title = '다운로드';
        
        downloadImg.src = '/img/download-icon.png';
        downloadImg.alt = '다운로드';
        downloadImg.classList.add('file-download-icon', 'img-btn');
        
        downloadEl.appendChild(downloadImg);
        fileItem.appendChild(downloadEl);
        
        // 파일 리스트에 추가
        fileListEl.appendChild(fileItem);
    });
}
// ============================================
// 조직도 모달
// ============================================
// 조직도 모달 열기
async function openApproverOrgModal() {
	// 이미 3명이면 추가 불가
	if (approverList.length >= MAX_APPROVERS) {
	    alert(`결재자는 최대 ${MAX_APPROVERS}명까지만 추가할 수 있습니다.`);
	    return;
	}
	
    // 조직도 데이터 로드
    await getApproverOrganizationChart();
    
    // 모달 열기
    const modalEl = document.getElementById('approver-org-modal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
    
    // 그리드 레이아웃 새로고침
    setTimeout(() => {
        if (approverTreeGrid) {
            approverTreeGrid.refreshLayout && approverTreeGrid.refreshLayout();
        }
    }, 200);
}


// 조직도 데이터 불러오기
async function getApproverOrganizationChart() {
    await fetch('/api/schedules/organizationChart', {method: 'GET'})
        .then(response => {
            if (!response.ok) throw new Error(response.text());
            return response.json();
        })
        .then(async data => {
            approverTreeData = await buildApproverTree(data.data);
            await renderApproverOrgGrid();
        })
        .catch(error => {
            console.error('에러', error);
            alert("조직 데이터 조회 실패");
        });
}

// 트리그리드 형태로 변환
async function buildApproverTree(flatList) {
    const deptMap = {};
    
    flatList.forEach(item => {
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
                posName: item.POS_NAME || '-',
                type: 'employee'
            });
        }
    });

    const treeRoot = [];
    Object.values(deptMap).forEach(dept => {
        if (!dept.parentId || !deptMap[dept.parentId]) {
            treeRoot.push(dept);
        } else {
            deptMap[dept.parentId].children.push(dept);
        }
    });

    return convertApproverTreeNodes(treeRoot);
}

// 트리 노드 변환
function convertApproverTreeNodes(nodes) {
    return nodes.map(node => {
        const newNode = { ...node };
        if (Array.isArray(newNode.children) && newNode.children.length > 0) {
            newNode._children = convertApproverTreeNodes(newNode.children);
        }
        delete newNode.children;
        newNode._attributes = { expanded: true };
        return newNode;
    });
}

// 조직도 그리드 그리기
async function renderApproverOrgGrid() {
    tui.Grid.setLanguage('ko', {
        display: {
            noData: '데이터가 없습니다.',
            loadingData: '데이터를 불러오는 중입니다.',
        },
        filter: {
            contains: '포함',
            eq: '일치',
            apply: '적용',
            clear: '초기화',
            selectAll: '전체 선택'
        }
    });

    // 기존 그리드 제거
    if (approverTreeGrid) {
        approverTreeGrid.destroy();
        approverTreeGrid = null;
    }

    approverTreeGrid = new tui.Grid({
        el: document.getElementById('approverOrgChartGrid'),
        data: approverTreeData,
        rowHeaders: ['checkbox'],
        bodyHeight: 'auto',
        filter: true,
        treeColumnOptions: {
            name: 'name',
            useCascadingCheckbox: true
        },
        columns: [
            { 
                header: '이름', 
                name: 'name', 
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
}

// ============================================
// 첨부파일 관리 함수
// ============================================

// 파일 목록 렌더링
async function renderFileList() {
    const fileListEl = document.getElementById('file-list');
    
    if (selectedFiles.length === 0) {
        fileListEl.innerHTML = '';
        return;
    }
    
    fileListEl.innerHTML = selectedFiles.map((file, index) => `
        <div class="file-item d-flex">
            <div class="file-info d-flex">
                <i class="bi bi-file-earmark file-icon"></i>
                <div class="d-flex">
                    <div class="file-name">${file.name}</div>
                    <div class="file-size ">${formatFileSize(file.size)}</div>
                </div>
            </div>
            <button type="button" class="btn btn-sm btn-outline-danger file-remove-btn" data-index="${index}">
                <i class="bi bi-x"></i>
            </button>
        </div>
    `).join('');
    
    // 삭제 버튼 이벤트 등록
    document.querySelectorAll('.file-remove-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const index = parseInt(this.dataset.index);
            removeFile(index);
        });
    });
}

// 파일 삭제
function removeFile(index) {
    selectedFiles.splice(index, 1);
    renderFileList();
}

// 파일 크기 포맷
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}


// ============================================
// 결재선 관리 함수
// ============================================

// 결재선 화면에 그리기
async function renderApproverList(readOnly = false) {
    const tbody = document.getElementById('approver-list');
	const actionHeader = document.getElementById('approver-action-header');
    
	if (readOnly) {
	    actionHeader.textContent = '상태';
	    actionHeader.style.width = '120px';
	} else {
	    actionHeader.textContent = '삭제';
	    actionHeader.style.width = '120px';
	}
	
	tbody.innerHTML = '';

    approverList.forEach((approver, index) => {
        const row = document.createElement('tr');
		if (readOnly) {
			const statusBadge = getStatusBadge(approver.status);
		    // ========== 읽기 전용 (버튼 없음) ==========
		    row.innerHTML = `
		        <td>${index + 1}차</td>
		        <td>${approver.posName || '-'}</td>
		        <td>${approver.empName} (${approver.empId})</td>
		        <td>${statusBadge}</td>
		    `;
		} else {
	        row.innerHTML = `
	            <td>${index + 1}차</td>
	            <td>${approver.posName}</td>
	            <td>${approver.empName} (${approver.empId})</td>
	            <td>
	                <button type="button" class="btn btn-sm btn-outline-secondary move-up-btn" data-index="${index}" ${index === 0 ? 'disabled' : ''}>
	                    <i class="bi bi-arrow-up"></i>
	                </button>
	                <button type="button" class="btn btn-sm btn-outline-secondary move-down-btn" data-index="${index}" ${index === approverList.length - 1 ? 'disabled' : ''}>
	                    <i class="bi bi-arrow-down"></i>
	                </button>
	                <button type="button" class="btn btn-sm btn-danger remove-approver-btn" data-index="${index}">
	                    <i class="bi bi-x"></i>
	                </button>
	            </td>
	        `;
		}
        tbody.appendChild(row);
    });

	if(readOnly) {
	    // 위로 이동 버튼
	    document.querySelectorAll('.move-up-btn').forEach(btn => {
	        btn.addEventListener('click', function () {
	            const index = parseInt(this.dataset.index);
	            moveApprover(index, -1);
	        });
	    });
	
	    // 아래로 이동 버튼
	    document.querySelectorAll('.move-down-btn').forEach(btn => {
	        btn.addEventListener('click', function () {
	            const index = parseInt(this.dataset.index);
	            moveApprover(index, 1);
	        });
	    });
	
	    // 삭제 버튼
	    document.querySelectorAll('.remove-approver-btn').forEach(btn => {
	        btn.addEventListener('click', function () {
	            const index = parseInt(this.dataset.index);
	            removeApprover(index);
	        });
	    });
	}
}

// 상태 배지
function getStatusBadge(status) {
    const statusMap = {
        'PENDING': '<span class="badge bg-warning">대기</span>',
        'APPROVED': '<span class="badge bg-success">승인</span>',
        'FINAL_APPROVED': '<span class="badge bg-primary">전결승인</span>',
        'REJECTED': '<span class="badge bg-danger">반려</span>',
        'SKIPPED': '<span class="badge bg-secondary">건너뜀</span>'
    };
    return statusMap[status] || '<span class="badge bg-secondary">-</span>';
}

// 결재자 순서 이동
function moveApprover(index, direction) {
    const newIndex = index + direction;
    
    if (newIndex < 0 || newIndex >= approverList.length) return;
    
    // 배열에서 위치 바꾸기
    [approverList[index], approverList[newIndex]] = [approverList[newIndex], approverList[index]];
    
    renderApproverList(false);
}

// 결재자 삭제
function removeApprover(index) {
    approverList.splice(index, 1);
    renderApproverList(false);
}


// ============================================
// 유효성 검사
// ============================================
async function validateApprovalForm() {
    const titleEl = document.getElementById('approval-title');
    const finishDateEl = document.getElementById('finish-date');
    const formType = document.getElementById('form-type-select').value;
    
    // 문서 제목 체크
    if (!titleEl.value.trim()) {
        alert('문서 제목을 입력해주세요.');
        titleEl.focus();
        return false;
    }
    
    // 결재완료기간 체크
    if (!finishDateEl.value) {
        alert('결재완료기간을 선택해주세요.');
        finishDateEl.focus();
        return false;
    }
    
    // 결재선 체크
    if (approverList.length === 0) {
        alert('결재권한자를 최소 1명 이상 추가해주세요.');
        return false;
    }
    
    if (approverList.length > 3) {
        alert('결재권한자는 최대 3명까지만 추가할 수 있습니다.');
        return false;
    }
    
    // 양식별 유효성 검사
    if (formType === 'leave') {
        const leaveType = document.getElementById('leave-type').value;
        const leaveStartDate = document.getElementById('leave-start-date').value;
        const leaveEndDate = document.getElementById('leave-end-date').value;
        
        if (!leaveType) {
            alert('휴가종류를 선택해주세요.');
            return false;
        }
        if (!leaveStartDate || !leaveEndDate) {
            alert('휴가기간을 선택해주세요.');
            return false;
        }
		
		// ========== 년도 체크 ==========
		const currentYear = new Date().getFullYear();
		const startYear = new Date(leaveStartDate).getFullYear();
		const endYear = new Date(leaveEndDate).getFullYear();

		if (startYear !== currentYear || endYear !== currentYear) {
		    alert(`휴가는 ${currentYear}년도 내에서만 신청 가능합니다.`);
		    return false;
		}

		// ========== 휴가 중복 체크 (비동기) ==========
		const isDuplicate = await checkLeaveDuplicate(leaveStartDate, leaveEndDate, leaveType);
		if (isDuplicate) {
		    return false;  // 중복이면 등록 중단
		}
		
    }
    
    if (formType === 'expense') {
        const expenseType = document.getElementById('expense-type').value;
        const expenseDate = document.getElementById('expense-date').value;
        const expenseVendor = document.getElementById('expense-vendor').value;
        const expenseAmount = document.getElementById('expense-amount').value;
        
        if (!expenseType) {
            alert('지출종류를 선택해주세요.');
            return false;
        }
        if (!expenseDate) {
            alert('지출일자를 선택해주세요.');
            return false;
        }
        if (!expenseVendor.trim()) {
            alert('지출처를 입력해주세요.');
            return false;
        }
        if (!expenseAmount || expenseAmount <= 0) {
            alert('금액을 입력해주세요.');
            return false;
        }
    }
    
    return true;
}

// 휴가 중복 체크 함수
async function checkLeaveDuplicate(startDate, endDate, leaveType) {
    try {
        const currentUserId = document.getElementById('currentUserId').value;
        
        const response = await fetch(
            apiUrl(`new/approval/check-leave-duplicate?leaveType=${leaveType}&startDate=${startDate}&endDate=${endDate}`),
            {
                method: 'GET',
                headers: {
                    [csrfHeader]: csrfToken
                }
            }
        );
        
        const result = await response.json();
        
        if (result.isDuplicate) {
			alert(`${result.message}\n기존 휴가: ${result.existingLeave}`);
//            alert(`선택하신 기간(${startDate} ~ ${endDate})에 이미 요청된 휴가가 있습니다.\n기존 휴가: ${result.existingLeave}`);
            return true;  // 중복
        }
        
        return false;  // 중복 아님
        
    } catch (error) {
        console.error('휴가 중복 체크 실패:', error);
        alert('휴가 중복 확인 중 오류가 발생했습니다.');
        return true;  // 에러 시 안전하게 중복으로 처리
    }
}

// ============================================
// 문서 등록
// ============================================

// FormData 수집
function collectFormData() {
    const formData = new FormData();
    
    // 기본 정보
    const formType = document.getElementById('form-type-select').value;
    const currentUserId = document.getElementById('currentUserId').value;
    
	// ApprovalDocumentDTO → JSON
	const documentData = {
	    formType: formType,
	    approvalTitle: document.getElementById('approval-title').value,
	    reason: document.getElementById('reason-write').value,
	    createdDate: document.getElementById('create-date').value,
	    finishDate: document.getElementById('finish-date').value,
	    empId: currentUserId
	};
	// Blob으로 변환 (Content-Type 지정 필수!)
	const documentBlob = new Blob([JSON.stringify(documentData)], {
	    type: 'application/json'
	});
	formData.append('document', documentBlob);
    
	// ApprovalLeaveDTO → JSON (휴가일 때만)
	if (formType === 'leave') {
	    const leaveData = {
	        leaveType: document.getElementById('leave-type').value,
	        leaveStartDate: document.getElementById('leave-start-date').value,
	        leaveEndDate: document.getElementById('leave-end-date').value,
	        leaveDays: parseFloat(document.getElementById('leave-days').value)
	    };
		const leaveBlob = new Blob([JSON.stringify(leaveData)], {
		    type: 'application/json'
		});
		formData.append('leave', leaveBlob);
	}
    
	// ApprovalExpenseDTO → JSON (지출일 때만)
	if (formType === 'expense') {
	    const expenseData = {
	        expenseType: document.getElementById('expense-type').value,
	        expenseDate: document.getElementById('expense-date').value,
	        expenseVendor: document.getElementById('expense-vendor').value,
	        expenseAmount: parseInt(document.getElementById('expense-amount').value)
	    };
		const expenseBlob = new Blob([JSON.stringify(expenseData)], {
		    type: 'application/json'
		});
		formData.append('expense', expenseBlob);
	}
    
	// List<ApprovalLineDTO> → JSON
	const approversData = approverList.map((approver, index) => ({
	    approverId: approver.empId,
	    approverName: approver.empName,
	    posName: approver.posName,
	    stepOrder: index + 1
	}));
	const approversBlob = new Blob([JSON.stringify(approversData)], {
	    type: 'application/json'
	});
	formData.append('approvers', approversBlob);
    
	// 첨부파일
	selectedFiles.forEach((file) => {
	    formData.append('attachments', file);
	});
    
    return formData;
}


// 결재문서 등록함수
async function submitApprovalDocument() {
    const formData = collectFormData();
	try {
	    const res = await fetch(apiUrl('/new/approval/create'), {
	        method: 'POST',
			headers: {
			    [csrfHeader]: csrfToken
			},
	        body: formData
	    })
	
		if (!res.ok) {
		    throw new Error('서버 오류가 발생했습니다.');
		}
		
		const result = await res.json();
		
		alert(result.message || '결재 문서가 등록되었습니다.');

		
		// 모달 닫고 새로고침
		setTimeout(() => {
		    approvalModal.hide();
			// 그리드 새로고침
			if (approvalGrid) {
			    approvalGrid.readData(1, { tab: currentTab }, true);
			}
//		    location.reload();  
		}, 300);
	} catch (error) {
        console.error('등록 에러:', error);
        alert(error.message || '요청 처리 중 오류가 발생했습니다.');
    }
}

// 전결, 결재 승인 처리 함수
async function approveDocument(isFinalApproval) {
    const confirmMessage = isFinalApproval 
        ? '전결 승인하시겠습니까?\n전결 승인 시 이후 결재 단계가 생략됩니다.' 
        : '승인하시겠습니까?';
    
    if (!confirm(confirmMessage)) return;
    
    if (!CURRENT_APPROVAL_ID) {  // ← 전역 변수 사용
        alert('결재 정보를 찾을 수 없습니다.');
        return;
    }
    
    try {
        const response = await fetch(apiUrl('new/approval/approve'), {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                approvalId: CURRENT_APPROVAL_ID,  // ← 전역 변수
                isFinalApproval: isFinalApproval
            })
        });
	    
	    if (!response.ok) throw new Error('승인 처리 실패');
	    
	    const result = await response.json();
	    
	    if (result.result) {
	        alert(isFinalApproval ? '전결 승인되었습니다.' : '승인되었습니다.');
	        approvalModal.hide();
	        
	        // 그리드 새로고침
	        if (approvalGrid) {
	            approvalGrid.readData(1, { tab: currentTab }, true);
	        }
	    } else {
	        alert(result.message || '승인 처리에 실패했습니다.');
	    }
		    
	} catch (error) {
	    console.error('승인 처리 에러:', error);
	    alert('승인 처리 중 오류가 발생했습니다.');
	}
}

// 반려 처리 함수
async function rejectDocument() {
    const reason = prompt('반려 사유를 입력해주세요:');
    
    if (!reason || reason.trim() === '') {
        alert('반려 사유를 입력해주세요.');
        return;
    }
    
    if (!CURRENT_APPROVAL_ID) {  // ← 전역 변수 사용
        alert('결재 정보를 찾을 수 없습니다.');
        return;
    }
    
    try {
        const response = await fetch(apiUrl('new/approval/reject'), {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                approvalId: CURRENT_APPROVAL_ID,  // ← 전역 변수
                rejectReason: reason
            })
        });
	    
	    if (!response.ok) throw new Error('반려 처리 실패');
	    
	    const result = await response.json();
	    
	    if (result.result) {
	        alert('반려되었습니다.');
	        approvalModal.hide();
	        
	        // 그리드 새로고침
	        if (approvalGrid) {
	            approvalGrid.readData(1, { tab: currentTab }, true);
	        }
	    } else {
	        alert(result.message || '반려 처리에 실패했습니다.');
	    }
	    
	} catch (error) {
	    console.error('반려 처리 에러:', error);
	    alert('반려 처리 중 오류가 발생했습니다.');
	}
}

// ============================================
// 이벤트 리스너 등록
// ============================================

// 사유내용 글자수 카운터
const reasonEl = document.getElementById('reason-write');
const reasonCountEl = document.getElementById('reason-char-count');

if (reasonEl && reasonCountEl) {
    reasonEl.addEventListener('input', function () {
        const currentLength = this.value.length;
        reasonCountEl.textContent = `${currentLength}/3000자`;
    });
}

// 양식 종류 변경 이벤트
document.getElementById('form-type-select').addEventListener('change', function () {
    toggleFormFields(this.value);
});

// 휴가 종류 변경 이벤트
document.getElementById('leave-type').addEventListener('change', function () {
    const leaveType = this.value;
    const endDateEl = document.getElementById('leave-end-date');
    const daysEl = document.getElementById('leave-days');
	const startDateEl = document.getElementById('leave-start-date');

    
    if (leaveType === '반차') {
		
		if (startDateEl.value) {
		    endDateEl.value = startDateEl.value;  // 시작일과 동일하게 설정
		} else {
		    endDateEl.value = '';  // 시작일이 없으면 빈 값으로 초기화
		}
        // 종료일 입력 비활성화
        endDateEl.disabled = true;
        endDateEl.style.backgroundColor = '#e9ecef';
        
        // 일수 고정 표시
        daysEl.value = 0.5;
        daysEl.readOnly = true;
        daysEl.style.backgroundColor = '#e9ecef';
        
    } else {
        // 연차 등은 정상 작동
        endDateEl.disabled = false;
        endDateEl.style.backgroundColor = '';
        
        daysEl.readOnly = true;  // 자동계산이니까 읽기전용 유지
        daysEl.style.backgroundColor = '#e9ecef';
    }
});

// 휴가 기간 변경 시 일수 자동 계산
document.getElementById('leave-type').addEventListener('change', function () {
    const leaveType = document.getElementById('leave-type').value;
    const startDateEl = document.getElementById('leave-start-date');
    const endDateEl = document.getElementById('leave-end-date');
    const daysEl = document.getElementById('leave-days');
    
    if (leaveType !== '연차') {
        endDateEl.value = startDateEl.value;  // 종료일 = 시작일
        daysEl.value = 0.5;
    } else {
        calculateLeaveDays();  // 기존 일수 계산 로직
    }
});
// 시작일 변경 시 반차면 종료일도 자동 설정
document.getElementById('leave-start-date').addEventListener('change', function () {
    const leaveType = document.getElementById('leave-type').value;
    const endDateEl = document.getElementById('leave-end-date');
    const daysEl = document.getElementById('leave-days');
    
    if (leaveType !== '연차') {
        endDateEl.value = this.value;  // 종료일 = 시작일
        daysEl.value = 0.5;
    } else {
        calculateLeaveDays();  // 기존 일수 계산 로직
    }
});
document.getElementById('leave-end-date').addEventListener('change', calculateLeaveDays);

// 파일 선택 버튼 클릭
document.getElementById('file-upload-btn').addEventListener('click', function () {
    document.getElementById('file-input').click();
});

// 파일 선택 시
document.getElementById('file-input').addEventListener('change', function (e) {
    const files = Array.from(e.target.files);
    
    // 파일 개수 체크
    if (selectedFiles.length + files.length > MAX_FILE_COUNT_APPROVAL) {
        alert(`최대 ${MAX_FILE_COUNT_APPROVAL}개 파일만 첨부 가능합니다.`);
        return;
    }
    
    // 파일 크기 체크
    for (let file of files) {
        if (file.size > MAX_FILE_SIZE) {
            alert(`${file.name}의 크기가 10MB를 초과합니다.`);
            return;
        }
    }
    
    // 선택된 파일 추가
    selectedFiles.push(...files);
    renderFileList();
    
    // input 초기화 (같은 파일 다시 선택 가능하게)
    e.target.value = '';
});



// 조직도에서 결재자 선택 버튼 - 추가
document.getElementById('open-approver-org-btn').addEventListener('click', function () {
    openApproverOrgModal();
});

// 선택한 직원 추가 버튼
document.getElementById('add-selected-approvers-btn').addEventListener('click', function () {
    if (!approverTreeGrid) return;

    const checkedRows = approverTreeGrid.getCheckedRows();
    const selectedEmployees = checkedRows.filter(row => row.type === 'employee');

    if (selectedEmployees.length === 0) {
        alert('선택된 직원이 없습니다.');
        return;
    }

    // 3명 초과 체크
    const remainingSlots = MAX_APPROVERS - approverList.length;
    if (selectedEmployees.length > remainingSlots) {
        alert(`결재자는 최대 ${MAX_APPROVERS}명까지만 추가할 수 있습니다.\n현재 ${approverList.length}명 등록됨, ${remainingSlots}명 추가 가능`);
        return;
    }

    let addedCount = 0;
    selectedEmployees.forEach(emp => {
        if (!approverList.find(item => item.empId === emp.empId)) {
            approverList.push({
                empId: emp.empId,
                empName: emp.name,
                posName: emp.posName
            });
            addedCount++;
        }
    });

    renderApproverList(false);

    if (addedCount > 0) {
        alert(`${addedCount}명의 결재자가 추가되었습니다.`);
    } else {
        alert('이미 추가된 결재자만 선택하셨습니다.');
    }

    const modalEl = document.getElementById('approver-org-modal');
    const modal = bootstrap.Modal.getInstance(modalEl);
    if (modal) modal.hide();
});

// 등록 버튼 이벤트 등록
const approvalForm = document.getElementById('approval-form');
if (approvalForm) {
    approvalForm.addEventListener('submit', async function (e) {
        e.preventDefault();  // 기본 submit 막기
		// 유효성 검사
		const isValid = await validateApprovalForm(); 
        if (!isValid) {
            return;
        }
        
        // FormData 생성 및 전송
        await submitApprovalDocument();
    });
}

// 승인 버튼
document.getElementById('approve-btn').addEventListener('click', () => {
	approveDocument(false); // 일반 승인
});

// 전결 승인 버튼  
document.getElementById('final-approve-btn').addEventListener('click', () => {
	approveDocument(true); // 전결 승인
});

// 반려 버튼
document.getElementById('reject-btn').addEventListener('click', () => {
	rejectDocument(); // 반려 처리
});

window.openApprovalModal = openApprovalModal;
console.log('✅ openApprovalModal loaded');