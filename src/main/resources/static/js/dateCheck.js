function check(type) {
    const start = prompt('증분처리의 시작날짜를 지정하세요.', '20200101');
    const regex = /^(202[0-3])(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])$/;

    if (!regex.test(start)) {
        alert('올바른 날짜 형식이 아닙니다. 다시 입력하세요');
        return false;
    }

    let now = new Date(Date.now()); // 현재 날짜
    const year = now.getFullYear(); // 현재 날짜의 연도
    const month = String(now.getMonth() + 1).padStart(2, '0');  // 현재 날짜의 달
    const day = String(now.getDate()).padStart(2, '0'); // 현재 날짜의 일
    let end = prompt('증분처리의 종료날짜를 지정하세요.', `${year}${month}${day}`);   // 기본값은 현재 날짜

    if (!regex.test(end)) {
        alert('올바른 날짜 형식이 아닙니다. 다시 입력하세요');
        return false;
    }

    location.href = '/jump/api?type=' + type + '&startDate=' + start + '&endDate=' + end;
    return true;
}