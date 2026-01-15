// ============================================
// 전역 변수
// ============================================


let selectedFiles = [];  // 선택된 파일 배열
const MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10MB
const MAX_FILE_COUNT = 5;

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

// 결재모달 열기 함수
function openApprovalModal(mode, options = {}) {
    // 1) 항상 먼저 초기화
    resetApprovalForm();

    const titleEl   = document.getElementById('approval-title');
    const reasonEl  = document.getElementById('reason-write');

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

		// 입력 필드 비활성화
		if (titleEl)  titleEl.readOnly = true;
		if (reasonEl) reasonEl.readOnly = true;

		// 결재선 추가 버튼 숨김
		document.getElementById('open-approver-org-btn').style.display = 'none';

		// 결재 권한 확인
		const currentUserId = document.getElementById('currentUserId').value;
		const isApprover = checkIfCurrentUserIsApprover(options.approvalId, currentUserId);

		if (isApprover) {
		    // 현재 사용자가 결재자면 결재 버튼 표시
		    approveBtn.style.display = '';
		    finalApproveBtn.style.display = '';
		    rejectBtn.style.display = '';
		} else {
		    // 아니면 결재 버튼 숨김
		    approveBtn.style.display = 'none';
		    finalApproveBtn.style.display = 'none';
		    rejectBtn.style.display = 'none';
		}

		// 서버에서 문서 데이터 로드
		loadApprovalDocument(options.approvalId);
    }

    approvalModal.show();  // 실제 모달 열기[web:40][web:55]
}


// 결재문서 모달 초기화
function resetApprovalForm() {
    const formEl          = document.querySelector('#approval-modal form');
    const titleEl         = document.getElementById('approval-title');
    const reasonEl        = document.getElementById('reason-write');
    const reasonCountEl   = document.getElementById('reason-char-count');
    const todayDateEl     = document.getElementById('today-date');
    const createDateEl    = document.getElementById('create-date');
    const finishDateEl    = document.getElementById('finish-date');

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
	    toggleFormFields('free');
	}

	// 휴가 필드 초기화
	document.getElementById('leave-type').value = '';
	document.getElementById('leave-start-date').value = '';
	document.getElementById('leave-end-date').value = '';
	document.getElementById('leave-days').value = '';

	// 지출 필드 초기화
	document.getElementById('expense-type').value = '';
	document.getElementById('expense-date').value = '';
	document.getElementById('expense-vendor').value = '';
	document.getElementById('expense-amount').value = '';
	

    // 4) 모드별 버튼 상태 초기값 (기본: 기안 모드 기준)
    const saveBtn              = document.getElementById('saveBtn');
    const approvalCheckBtn     = document.getElementById('approvalCheckBtn');
    const approvalCompanionBtn = document.getElementById('approvalCompanionBtn');

    if (saveBtn) saveBtn.style.display = '';
    if (approvalCheckBtn) approvalCheckBtn.style.display = 'none';
    if (approvalCompanionBtn) approvalCompanionBtn.style.display = 'none';
	
	// 결재선 초기화
	approverList = [];
	renderApproverList();
	
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
function toggleFormFields(formType) {
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
    
	// 반차면 일수 계산하지 않음
	if (leaveType === '반차') {
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
function renderFileList() {
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
function renderApproverList() {
    const tbody = document.getElementById('approver-list');
    
    const rows = tbody.querySelectorAll('tr:not(:first-child)');
    rows.forEach(row => row.remove());

    approverList.forEach((approver, index) => {
        const row = document.createElement('tr');
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
            </td>
            <td>
                <button type="button" class="btn btn-sm btn-danger remove-approver-btn" data-index="${index}">
                    <i class="bi bi-x"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });

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

// 결재자 순서 이동
function moveApprover(index, direction) {
    const newIndex = index + direction;
    
    if (newIndex < 0 || newIndex >= approverList.length) return;
    
    // 배열에서 위치 바꾸기
    [approverList[index], approverList[newIndex]] = [approverList[newIndex], approverList[index]];
    
    renderApproverList();
}

// 결재자 삭제
function removeApprover(index) {
    approverList.splice(index, 1);
    renderApproverList();
}


// ============================================
// 유효성 검사
// ============================================
function validateApprovalForm() {
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


// 서버로 전송함수
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
//		    location.reload();  
		}, 300);
	} catch (error) {
        console.error('등록 에러:', error);
        alert(error.message || '요청 처리 중 오류가 발생했습니다.');
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
// 시작일 변경 시 반차면 종료일도 자동 설정
document.getElementById('leave-start-date').addEventListener('change', function () {
    const leaveType = document.getElementById('leave-type').value;
    const endDateEl = document.getElementById('leave-end-date');
    const daysEl = document.getElementById('leave-days');
    
    if (leaveType === '반차') {
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
    if (selectedFiles.length + files.length > MAX_FILE_COUNT) {
        alert(`최대 ${MAX_FILE_COUNT}개 파일만 첨부 가능합니다.`);
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

    renderApproverList();

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
    approvalForm.addEventListener('submit', function (e) {
        e.preventDefault();  // 기본 submit 막기
		// 유효성 검사
        if (!validateApprovalForm()) {
            return;
        }
        
        // FormData 생성 및 전송
        submitApprovalDocument();
    });
}