package uk.gov.hmrc.timetopay.arrangement.models

import java.time.LocalDate

case class TTPArrangement(id: Option[String],
                          createdOn: Option[LocalDate],
                          paymentPlanReference: String,
                          directDebitReference: String,
                          taxpayer: Taxpayer,
                          schedule: Schedule,
                          desArrangement : Option[DesSubmissionRequest]) {

}

case class DesTTPArrangement(startDate: LocalDate,
                               endDate: LocalDate,
                               firstPaymentDate: LocalDate,
                               firstPaymentAmount: String,
                               regularPaymentAmount: String,
                               regularPaymentFrequency: String,
                               reviewDate: LocalDate,
                               initials: String,
                               enforcementAction: String,
                               directDebit: Boolean,
                               debitDetails: List[Debit],
                               saNote: String) {

}

case class LetterAndControl(customerName: String,
                            salutation: String,
                            addressLine1: String,
                            addressLine2: String,
                            addressLine3: String,
                            addressLine4: String,
                            addressLine5: String,
                            postCode: String,
                            totalAll: String,
                            clmIndicateInt: String,
                            clmPymtString: String,
                            officeName1: String,
                            officeName2: String,
                            officePostcode: String,
                            officePhone: String,
                            officeFax: String,
                            officeOpeningHours: String,
                            template: String,
                            exceptionType: Option[String] = None,
                            exceptionReason: Option[String] = None
                           ) {

}

case class DesSubmissionRequest(ttpArrangement: DesTTPArrangement, letterAndControl: LetterAndControl) {}
