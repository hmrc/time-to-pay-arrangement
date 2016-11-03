package uk.gov.hmrc.timetopay.arrangement.models

import java.time.LocalDate


case class Schedule(startDate: LocalDate,
                    endDate: LocalDate,
                    initialPayment: BigDecimal,
                    amountToPay: BigDecimal,
                    instalmentBalance: BigDecimal,
                    totalInterestCharged: BigDecimal,
                    totalPayable: BigDecimal,
                    instalments: List[Instalment]) {

}

case class Instalment (paymentDate: LocalDate, amount: BigDecimal) {

}
