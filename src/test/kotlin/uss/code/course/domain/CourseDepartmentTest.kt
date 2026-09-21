package uss.code.course.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.global.exception.domain.ExceptionCode.INVALID_DEPARTMENT
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.ExceptionCode.INVALID_INTERDISCIPLINARY_DEPARTMENT
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest
import uss.code.member.domain.MemberDepartment

@IntegrationTest
class CourseDepartmentTest {
    @Nested
    inner class 코드값_변환_테스트 {
        @Test
        fun 연계_API의_학과_코드를_상수로_변환한다() {
            //given

            //when & then
            assertThat(CourseDepartment.fromCode("0000077")).isEqualTo(CourseDepartment.COMPUTER_ENGINEERING)
            assertThat(CourseDepartment.fromCode("AIA1")).isEqualTo(CourseDepartment.KOREAN_LITERATURE)
            assertThat(CourseDepartment.fromCode("XAA0")).isEqualTo(CourseDepartment.GENERAL_EDUCATION)
        }

        @Test
        fun 이번_학기에_새로_생긴_학과도_변환된다() {
            //given

            //when & then
            assertThat(CourseDepartment.fromCode("0000913")).isEqualTo(CourseDepartment.GLOBAL_TRADE_SERVICE)
            assertThat(CourseDepartment.fromCode("0000912")).isEqualTo(CourseDepartment.INTELLIGENT_ROBOT_SYSTEM)
        }

        @Test
        fun 상수명이_바뀌어도_학교_코드는_같은_상수로_변환된다() {
            //given

            //when & then
            assertThat(CourseDepartment.fromCode("0000813")).isEqualTo(CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL)
            assertThat(CourseDepartment.fromCode("0000156")).isEqualTo(CourseDepartment.CIVIL_ENVIRONMENT_ENGINEERING_MAJOR)
            assertThat(CourseDepartment.fromCode("0000183")).isEqualTo(CourseDepartment.LIFE_SCIENCE_SCHOOL)
            assertThat(CourseDepartment.fromCode("0000184")).isEqualTo(CourseDepartment.LIFE_SCIENCE_MAJOR)
            assertThat(CourseDepartment.fromCode("0000832")).isEqualTo(CourseDepartment.IBE_MAJOR)
        }

        @Test
        fun 폐지된_학과와_후신_학부는_서로_다른_코드를_유지한다() {
            //given

            //when & then
            assertThat(CourseDepartment.fromCode("EPC1")).isEqualTo(CourseDepartment.ELECTRONICS_ENGINEERING)
            assertThat(CourseDepartment.fromCode("0000813")).isEqualTo(CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL)
        }

        @Test
        fun 정의되지_않은_코드면_예외가_발생한다() {
            //given
            val unknownCode = "9999999"

            //when & then
            assertThatThrownBy { CourseDepartment.fromCode(unknownCode) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }

        @Test
        fun 빈_코드는_개설되지_않은_학과로_매칭되지_않는다() {
            //given
            // 무역학부, 국제개발협력연계전공은 이번 학기 개설이 없어 code가 빈 문자열이다
            val blankCode = ""

            //when & then
            assertThatThrownBy { CourseDepartment.fromCode(blankCode) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }

        @Test
        fun 코드가_없는_상수도_이름으로는_찾을_수_있다() {
            //given

            //when & then
            assertThat(CourseDepartment.from("TRADE")).isEqualTo(CourseDepartment.TRADE)
            assertThat(CourseDepartment.TRADE.code).isEmpty()
        }
    }

    @Nested
    inner class 소속_매핑_테스트 {
        @Test
        fun 학부_소속에는_학부와_하위_전공이_모두_포함된다() {
            //given
            val electronicsEngineering = MemberDepartment.ELECTRONICS_ENGINEERING_SCHOOL

            //when
            val departments = CourseDepartment.ownedBy(electronicsEngineering)

            //then
            assertThat(departments).containsExactlyInAnyOrder(
                CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL,
                CourseDepartment.ELECTRONICS_ENGINEERING_MAJOR,
                CourseDepartment.SEMICONDUCTOR_CONVERGENCE_MAJOR,
                CourseDepartment.ELECTRONICS_ENGINEERING,
            )
        }

        @Test
        fun 폐지된_학과의_강의는_후신_학부_소속으로_잡힌다() {
            //given

            //when & then
            assertThat(CourseDepartment.ELECTRONICS_ENGINEERING.owner)
                .isEqualTo(MemberDepartment.ELECTRONICS_ENGINEERING_SCHOOL)
            assertThat(CourseDepartment.TRADE.owner)
                .isEqualTo(MemberDepartment.GLOBAL_TRADE_SERVICE)
        }

        @Test
        fun 학부와_전공이_이름이_달라도_같은_소속으로_묶인다() {
            //given
            val urbanEnvironment = MemberDepartment.URBAN_ENVIRONMENT_ENGINEERING_SCHOOL

            //when
            val departments = CourseDepartment.ownedBy(urbanEnvironment)

            //then
            assertThat(departments).containsExactlyInAnyOrder(
                CourseDepartment.URBAN_ENVIRONMENT_ENGINEERING_SCHOOL,
                CourseDepartment.CIVIL_ENVIRONMENT_ENGINEERING_MAJOR,
                CourseDepartment.ENVIRONMENT_ENGINEERING_MAJOR,
            )
        }

        @Test
        fun 학생_소속이_아닌_값은_어느_소속에도_묶이지_않는다() {
            //given

            //when & then
            assertThat(CourseDepartment.GENERAL_EDUCATION.hasOwner()).isFalse()
            assertThat(CourseDepartment.TEACHING.hasOwner()).isFalse()
            assertThat(CourseDepartment.GENERAL_ELECTIVE.hasOwner()).isFalse()
            assertThat(CourseDepartment.MILITARY.hasOwner()).isFalse()
            assertThat(CourseDepartment.FUTURE_AUTOMOBILE.hasOwner()).isFalse()
            assertThat(CourseDepartment.HUSS_OTHER_UNIVERSITY.hasOwner()).isFalse()
        }

        @Test
        fun 대응되는_강의_학과가_없는_소속은_빈_목록을_반환한다() {
            //given

            //when & then
            assertThat(CourseDepartment.ownedBy(MemberDepartment.INTERNATIONAL_LIBERAL_ARTS)).isEmpty()
            assertThat(CourseDepartment.ownedBy(MemberDepartment.CONVERGENCE)).isEmpty()
        }

        @Test
        fun 동북아_세_전공은_각각_독립된_소속이다() {
            //given

            //when & then
            assertThat(CourseDepartment.ownedBy(MemberDepartment.NORTHEAST_ASIAN_TRADE_MAJOR))
                .containsExactly(CourseDepartment.NORTHEAST_ASIAN_TRADE_MAJOR)
            assertThat(CourseDepartment.ownedBy(MemberDepartment.IBE_MAJOR))
                .containsExactly(CourseDepartment.IBE_MAJOR)
        }
    }

    @Nested
    inner class 연계전공_판정_테스트 {
        @Test
        fun 연계전공이면_그대로_반환한다() {
            //given
            val department = "SOCIAL_DATA_SCIENCE"

            //when
            val courseDepartment = CourseDepartment.fromInterdisciplinary(department)

            //then
            assertThat(courseDepartment).isEqualTo(CourseDepartment.SOCIAL_DATA_SCIENCE)
        }

        @Test
        fun 새로_추가된_지능형로봇시스템도_연계전공으로_인정된다() {
            //given
            val department = "INTELLIGENT_ROBOT_SYSTEM"

            //when
            val courseDepartment = CourseDepartment.fromInterdisciplinary(department)

            //then
            assertThat(courseDepartment).isEqualTo(CourseDepartment.INTELLIGENT_ROBOT_SYSTEM)
        }

        @Test
        fun 연계전공이_아니면_예외가_발생한다() {
            //given
            val department = "COMPUTER_ENGINEERING"

            //when & then
            assertThatThrownBy { CourseDepartment.fromInterdisciplinary(department) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_INTERDISCIPLINARY_DEPARTMENT)
        }

        @Test
        fun 존재하지_않는_학과명이면_예외가_발생한다() {
            //given
            val department = "UNKNOWN_DEPARTMENT"

            //when & then
            assertThatThrownBy { CourseDepartment.fromInterdisciplinary(department) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }
    }

    @Nested
    inner class 학과_판정_테스트 {
        @Test
        fun 학과_갈래는_실측_드롭다운과_같은_76건이다() {
            //when
            val departments = CourseDepartment.departmentValues()

            //then
            assertThat(departments).hasSize(76)
        }

        @Test
        fun 연계전공_갈래는_실측_드롭다운과_같은_32건이다() {
            //when
            val interdisciplinary = CourseDepartment.interdisciplinaryValues()

            //then
            assertThat(interdisciplinary).hasSize(32)
        }

        @Test
        fun 학과면_그대로_반환한다() {
            //given
            val department = "COMPUTER_ENGINEERING"

            //when
            val courseDepartment = CourseDepartment.fromDepartment(department)

            //then
            assertThat(courseDepartment).isEqualTo(CourseDepartment.COMPUTER_ENGINEERING)
        }

        @Test
        fun HUSS_두_건은_연계전공이_아니라_학과다() {
            //when & then
            assertThat(CourseDepartment.fromDepartment("HUSS_OTHER_UNIVERSITY"))
                .isEqualTo(CourseDepartment.HUSS_OTHER_UNIVERSITY)
            assertThat(CourseDepartment.fromDepartment("HUSS_INCLUSIVE_SOCIETY_INITIATIVE"))
                .isEqualTo(CourseDepartment.HUSS_INCLUSIVE_SOCIETY_INITIATIVE)
        }

        @Test
        fun 연계전공을_넘기면_예외가_발생한다() {
            //given
            val interdisciplinary = "LOGISTICS"

            //when & then
            assertThatThrownBy { CourseDepartment.fromDepartment(interdisciplinary) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_DEPARTMENT)
        }

        @Test
        fun 교양처럼_학과가_아닌_값을_넘기면_예외가_발생한다() {
            //given
            val generalEducation = "GENERAL_EDUCATION"

            //when & then
            assertThatThrownBy { CourseDepartment.fromDepartment(generalEducation) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_DEPARTMENT)
        }

        @Test
        fun 폐지된_학과는_목록에_없지만_소속_매핑에는_남는다() {
            //when & then
            assertThat(CourseDepartment.departmentValues())
                .isNotEmpty()
                .doesNotContain(CourseDepartment.TRADE)
            assertThat(CourseDepartment.ownedBy(MemberDepartment.GLOBAL_TRADE_SERVICE))
                .contains(CourseDepartment.TRADE)
        }
    }

    @Nested
    inner class 실측_표기_일치_테스트 {
        @Test
        fun 쉼표가_들어간_연계전공은_쉼표를_그대로_담는다() {
            //when & then
            assertThat(CourseDepartment.MICE_SPORTS_TOURISM.displayName).isEqualTo("MICE,스포츠및관광연계전공")
            assertThat(CourseDepartment.CLIMATE_ENERGY_ENVIRONMENT.displayName).isEqualTo("기후,에너지및환경연계전공")
        }

        @Test
        fun 연계전공의_가운뎃점은_U00B7이다() {
            //given
            val middleDot = "\u00B7"
            val hangulLetterAraea = "\u318D"

            //when & then
            assertThat(CourseDepartment.BIO_CONVERGENCE_STARTUP.displayName)
                .isEqualTo("바이오융합${middleDot}창업연계전공")
                .doesNotContain(hangulLetterAraea)
            assertThat(CourseDepartment.AI_STARTUP.displayName)
                .isEqualTo("인공지능${middleDot}창업연계전공")
                .doesNotContain(hangulLetterAraea)
        }

        @Test
        fun 야간_학과는_접미를_그대로_담는다() {
            //when & then
            assertThat(CourseDepartment.ECONOMICS_NIGHT.displayName).isEqualTo("경제학과(야)")
            assertThat(CourseDepartment.TRADE_NIGHT.displayName).isEqualTo("무역학부(야)")
            assertThat(CourseDepartment.ECONOMICS_NIGHT.isNight()).isTrue()
        }

        @Test
        fun 학사_코드가_없는_신설_항목도_이름으로_찾을_수_있다() {
            //when & then
            assertThat(CourseDepartment.from("ANTIBODY_ENGINEERING").displayName).isEqualTo("항체공학연계전공")
            assertThat(CourseDepartment.ANTIBODY_ENGINEERING.code).isEmpty()
        }
    }
}
