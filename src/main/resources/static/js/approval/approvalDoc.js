// 로그인 사용자 사번, 이름
const LOGIN_USER_ID = document.getElementById('currentUserId').value;
const LOGIN_USER_NAME = document.getElementById('currentUserName').value;
//console.log('User ID:', LOGIN_USER_ID);
//console.log('User Name:', LOGIN_USER_NAME);

let approvalGrid = null;
let currentTab = 'all';
// 결재문서 그리드 EL
const approvalGridEl = document.getElementById('approvalGrid');

// 결재문서 모달
//const approvalModalEl = document.getElementById('approval-modal');
//const approvalModal   = new bootstrap.Modal(approvalModalEl);

// ============================================
// 초기 실행
// ============================================
document.addEventListener('DOMContentLoaded', function() {
	initApprovalGrid();
    setupTabEvents();
    
    // 기본 탭 활성화 (전체 결재)
    document.getElementById('every-approval').classList.add('active');
    loadEveryApproval();
});


// 기안 버튼
const createApprovalDocBtn = document.getElementById('writeBtn');

createApprovalDocBtn.addEventListener('click', function (event) {
    event.preventDefault();
    openApprovalModal('create');
});

// 조회 버튼 이벤트
document.getElementById('searchBtn').addEventListener('click', function() {
    searchApproval();
});

function searchApproval() {
    const startDate = document.getElementById('searchStartDate').value;
    const endDate = document.getElementById('searchEndDate').value;
    const keyword = document.getElementById('searchEmpIdAndformType').value;
    
    // Grid에 검색 조건 전달
    approvalGrid.readData(1, {
        tab: currentTab,
        searchStartDate: startDate,
        searchEndDate: endDate,
        searchKeyword: keyword
    }, true);
}

// ============================================
// 탭 버튼 이벤트 등록
// ============================================
function setupTabEvents() {
    // 전체 결재
    document.getElementById('every-approval').addEventListener('click', function() {
        setActiveTab(this);
        loadEveryApproval();
    });
    
    // 내 기안
    document.getElementById('my-approval').addEventListener('click', function() {
        setActiveTab(this);
        loadMyApproval();
    });
    
    // 결재 대기
    document.getElementById('wait-approval').addEventListener('click', function() {
        setActiveTab(this);
        loadWaitApproval();
    });
    
    // 결재 완료
    document.getElementById('complete-approval').addEventListener('click', function() {
        setActiveTab(this);
        loadCompleteApproval();
    });
}

// 탭 활성화 처리
function setupTabEvents() {
    document.getElementById('every-approval').addEventListener('click', function() {
        setActiveTab(this);
        loadEveryApproval();
    });
    
    document.getElementById('my-approval').addEventListener('click', function() {
        setActiveTab(this);
        loadMyApproval();
    });
    
    document.getElementById('wait-approval').addEventListener('click', function() {
        setActiveTab(this);
        loadWaitApproval();
    });
    
    document.getElementById('complete-approval').addEventListener('click', function() {
        setActiveTab(this);
        loadCompleteApproval();
    });
}

function setActiveTab(clickedTab) {
    // 모든 탭 비활성화
    document.querySelectorAll('.nav-tabs .nav-link').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 클릭한 탭만 활성화
    clickedTab.classList.add('active');
}


// ============================================
// 각 탭별 데이터 로드 함수
// ============================================

// 전체 결재
function loadEveryApproval() {
	currentTab = 'all';

    console.log('전체 결재 로드');
	approvalGrid.readData(1, { tab: 'all' }, true);
}

// 내 기안
function loadMyApproval() {
	currentTab = 'my';

    console.log('내 기안 로드');
	approvalGrid.readData(1, { tab: 'my' }, true);
}

// 결재 대기
function loadWaitApproval() {
	currentTab = 'pending';

    console.log('결재 대기 로드');
	approvalGrid.readData(1, { tab: 'pending' }, true);
}

// 결재 완료
function loadCompleteApproval() {
	currentTab = 'completed';

    console.log('결재 완료 로드');
	approvalGrid.readData(1, { tab: 'completed' }, true);
}


// ============================================
// 탭별 데이터 로드 함수
// ============================================
// 전체 결재
async function loadEveryApproval() {
	currentTab = 'all';
	approvalGrid.readData(1, { tab: 'all' }, true);
}

// 내 기안
async function loadMyApproval() {
	currentTab = 'my';
	approvalGrid.readData(1, { tab: 'my' }, true);
}

// 결재 대기
async function loadWaitApproval() {
	currentTab = 'pending';
	approvalGrid.readData(1, { tab: 'pending' }, true);
}

// 결재 완료
async function loadCompleteApproval() {
	currentTab = 'completed';
	approvalGrid.readData(1, { tab: 'completed' }, true);
}

// ============================================
// Toast UI Grid 초기화
// ============================================
function initApprovalGrid() {
    tui.Grid.setLanguage('ko', {
        display: {
            noData: '데이터가 없습니다.',
            loadingData: '데이터를 불러오는 중입니다.',
        }
    });
    
    if (approvalGrid) {
        approvalGrid.destroy();
    }
    
    approvalGrid = new tui.Grid({
        el: document.getElementById('approvalGrid'),
        data: {
			api: {
			    readData: {
			        url: apiUrl('new/approval/list'),
			        method: 'GET',
			        initParams: { 
			            tab: 'all'  // 초기 탭
			        }
		    	}
			},
			contentType: 'application/json',
			headers: {
			    [csrfHeader]: csrfToken
			}
		},  
		pageOptions: {
		    useClient: false,  // 서버 페이지네이션
		    perPage: 20,
		    page: 1
		},
        columns: [
//            {
//                header: '문서번호',
//                name: 'approvalId',
//                width: 100,
//                align: 'center'
//            },
            {
                header: '양식종류',
                name: 'formType',
                width: 100,
                align: 'center',
                formatter: function({value}) {
                    const typeMap = {
                        'free': '자유양식',
                        'leave': '휴가신청',
                        'expense': '지출결의서'
                    };
                    return typeMap[value] || value;
                }
            },
            {
                header: '문서제목',
                name: 'approvalTitle',
                width: 300,
                align: 'left'
            },
            {
                header: '기안자',
                name: 'empName',
                width: 100,
                align: 'center'
            },
            {
                header: '기안일',
                name: 'createdDate',
                width: 110,
                align: 'center'
            },
            {
                header: '결재완료기한',
                name: 'finishDate',
                width: 110,
                align: 'center'
            },
            {
                header: '상태',
                name: 'status',
                width: 100,
                align: 'center',
                formatter: function({value}) {
                    const statusMap = {
                        'PENDING': '<span class="badge bg-warning">대기</span>',
                        'APPROVED': '<span class="badge bg-success">승인</span>',
                        'REJECTED': '<span class="badge bg-danger">반려</span>'
                    };
                    return statusMap[value] || value;
                }
            },
			{
			    header: '상세',
			    name: 'actions',
			    width: 80,
			    align: 'center',
			    formatter: () => {
			        return '<button type="button" class="btn btn-sm btn-primary approval-detail-btn">상세</button>';
			    }
			}
        ],
        rowHeaders: ['rowNum'],
        bodyHeight: 600,
        rowHeight: 40
    });
    
    // 행 클릭 이벤트
	approvalGrid.on('click', (ev) => {
	    //'actions' 컬럼인지 확인
	    if (ev.columnName !== 'actions') {
	        return;
	    }

	    // 2) 실제로 버튼이 클릭됐는지 확인 (셀 안에 다른게 있을 수도 있으니)
	    const target = ev.nativeEvent.target;
	    if (!target.classList.contains('approval-detail-btn')) {
	        return;
	    }

	    // 3) 행 데이터 가져와서 모달 열기
	    const rowData = approvalGrid.getRow(ev.rowKey);
	    if (!rowData) return;

	    openApprovalModal('view', { approvalId: rowData.approvalId });
	});
}


































