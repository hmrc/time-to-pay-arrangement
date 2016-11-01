package uk.gov.hmrc.timetopay.arrangement.models

import java.time.LocalDate


case class Taxpayer(selfAssessment: SelfAssessment) {

}

case class SelfAssessment(utr: String,
                          addresses: List[Address],
                          communicationPreferences: CommunicationPreferences,
                          debits: List[Debit]) {
}

case class Address(addressLine1: String,
                   addressLine2: String,
                   addressLine3: String,
                   addressLine4: String,
                   addressLine5: String,
                   postCode: String) {

}

case class CommunicationPreferences( welshLanguageIndicator: Boolean,
                                    audioIndicator: Boolean,
                                    largePrintIndicator: Boolean,
                                    brailleIndicator: Boolean) {

}

case class Debit(debitType: String, dueDate: LocalDate) {}
