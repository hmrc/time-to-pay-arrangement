/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement.repository

import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.support.ITSpec
import uk.gov.hmrc.timetopay.arrangement.repository.TestDataTtp.arrangement
class TTPArrangementRepositorySpec extends ITSpec {
  val logger: Logger = Logger(getClass)

  private val arrangementRepo = fakeApplication.injector.instanceOf[TTPArrangementRepository]

  override def beforeEach(): Unit = {
    arrangementRepo.collection.drop(false).futureValue
    ()
  }

  override def afterEach(): Unit = {
    arrangementRepo.collection.drop(false).futureValue
    ()
  }

  "should add save a TTPArrangement" in {

    val result = arrangementRepo.doInsert(arrangement).futureValue
    result.get.taxpayer.selfAssessment.utr shouldBe arrangement.taxpayer.selfAssessment.utr

  }

  "should get a TTPArrangement for given id" in {

    logger.warn(arrangement.toString)
    arrangementRepo.doInsert(arrangement).futureValue

    val loaded = arrangementRepo.findByIdLocal(arrangement.id.get).futureValue.get
    assert(loaded.toString.contains("desArrangement"))
    assert(loaded.toString.contains("XXX-XXX-XXX"))
  }

  "should not save any personal data in" in {
    arrangementRepo.doInsert(arrangement).futureValue

    val loaded = arrangementRepo.findByIdLocal(arrangement.id.get).futureValue.get
    assert(!loaded.toString.contains("Customer Name"))
    assert(!loaded.toString.contains("addresses"))
  }
}
