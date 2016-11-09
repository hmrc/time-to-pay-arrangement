package uk.gov.hmrc.timetopay.arrangement.repositories


import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, GivenWhenThen, BeforeAndAfter, FunSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.MongoConnector
import uk.gov.hmrc.timetopay.arrangement.models.{TTPArrangement}

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import scala.io.Source


class TTPArrangementRepositoryISpec extends FunSpec with BeforeAndAfter with GivenWhenThen with ScalaFutures with MustMatchers {

  private def databaseName = "test-" + this.getClass.getSimpleName
  private def mongoUri: String = s"mongodb://127.0.0.1:27017/$databaseName"
  implicit val mongoConnectorForTest = new MongoConnector(mongoUri)
  implicit val mongo = mongoConnectorForTest.db

  val repository =  new TTPArrangementRepository

  before {
    clear()
  }

  after {
    clear()
  }

  def clear(): Unit = {
   repository.removeAll().futureValue
  }

  it("should add save a TTPArrangement") {
    val arrangement = Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/TTPArrangementResponse.json").getLines.mkString).as[TTPArrangement]

    val result = repository.save(arrangement).futureValue

    result.get.taxpayer.customerName mustBe arrangement.taxpayer.customerName

  }

  it("should get a TTPArrangement for given id") {
    val arrangement = Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/TTPArrangementResponse.json").getLines.mkString).as[TTPArrangement]
    repository.save(arrangement).futureValue

    val loaded = repository.findById(arrangement.id.get).futureValue.get
    loaded.id.get mustBe arrangement.id.get

  }


}
