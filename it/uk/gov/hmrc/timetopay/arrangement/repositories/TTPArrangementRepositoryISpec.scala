package uk.gov.hmrc.timetopay.arrangement.repositories


import org.scalatest.{MustMatchers, GivenWhenThen, BeforeAndAfter, FunSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.MongoConnector
import uk.gov.hmrc.timetopay.arrangement.{TTPArrangementRepository, TTPArrangement}
import uk.gov.hmrc.timetopay.arrangement.modelFormat._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._


trait FutureHelpers {
  implicit class futureHelpers[T](f: Future[T]) {
    def waitFor(timeout: FiniteDuration = 10 seconds) = Await.result(f, timeout)
  }
}


class TTPArrangementRepositoryISpec extends FunSpec with BeforeAndAfter with GivenWhenThen with FutureHelpers with MustMatchers {

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
   repository.removeAll().waitFor()
  }

  it("should add save a TTPArrangement") {
    val arrangement = Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/TTPArrangementResponse.json").getLines.mkString).as[TTPArrangement]

    val result = repository.save(arrangement).waitFor()

    result.get.taxpayer.customerName mustBe arrangement.taxpayer.customerName

  }

  it("should get a TTPArrangement for given id") {
    val arrangement = Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/TTPArrangementResponse.json").getLines.mkString).as[TTPArrangement]
    repository.save(arrangement).waitFor()

    val loaded = repository.findById(arrangement.id.get).waitFor().get
    loaded.id.get mustBe arrangement.id.get

  }


}
