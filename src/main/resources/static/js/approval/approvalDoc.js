// 로그인 사용자 사번, 이름
const LOGIN_USER_ID = document.getElementById('currentUserId').value;
const LOGIN_USER_NAME = document.getElementById('currentUserName').value;
//console.log('User ID:', LOGIN_USER_ID);
//console.log('User Name:', LOGIN_USER_NAME);
// 결재문서 그리드 EL
const approvalGridEl = document.getElementById('approvalGrid');

// 결재문서 모달
const approvalModalEl = document.getElementById('approval-modal');
const approvalModal   = new bootstrap.Modal(approvalModalEl);


// 기안 버튼
const createApprovalDocBtn = document.getElementById('writeBtn');

createApprovalDocBtn.addEventListener('click', function (event) {
    event.preventDefault();
    openApprovalModal('create');
});






































