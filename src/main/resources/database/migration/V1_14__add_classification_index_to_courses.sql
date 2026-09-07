-- 교양 강의 조회(GET /api/v1/courses/general-education)가 이수영역이 아니라 이수구분 코드로 바뀌면서
-- 기존 idx_area_sort가 쓰이지 않게 된다. 등호 필터 classification_code를 선두에 두고,
-- 뒤 두 컬럼이 ORDER BY grade_code, classification_code, haksu_code 의 나머지와 순서가 같아
-- 정렬을 인덱스로 대신한다.
ALTER TABLE courses ADD INDEX idx_classification_sort (classification_code, grade_code, haksu_code);
