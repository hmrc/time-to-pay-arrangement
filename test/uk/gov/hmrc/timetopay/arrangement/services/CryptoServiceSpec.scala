/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.timetopay.arrangement.services

import uk.gov.hmrc.timetopay.arrangement.model.{DesSubmissionRequest, PaymentSchedule, SelfAssessment, TTPArrangement, Taxpayer}
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, TestData}

import java.time.LocalDate

class CryptoServiceSpec extends ITSpec with TestData {

  val cryptoService: CryptoService = fakeApplication().injector.instanceOf[CryptoService]

  "check encrypted -> decripted match" in {
    val des: DesSubmissionRequest = DesSubmissionRequest(submitArrangementTTPArrangement, submitArrangementLetterAndControl)
    val request: TTPArrangement = TTPArrangement(
      paymentPlanReference = "",
      directDebitReference = "",
      taxpayer             = Taxpayer("", List.empty, SelfAssessment("", None, List.empty)),
      bankDetails          = bankDetails,
      schedule             = PaymentSchedule(LocalDate.now(), LocalDate.now(), 0, 0, 0, 0, 0, List.empty),
      desArrangement       = Some(des)
    )

    val encrypted = cryptoService.encryptTtpa(request)
    val decripted = cryptoService.decryptTtpa(encrypted)

    decripted shouldBe Some(request)
  }

}
