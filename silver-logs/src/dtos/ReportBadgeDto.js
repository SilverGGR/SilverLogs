export default class ReportBadgeDto{
  /**
   * @param {LocalDate} weekStart
   * @param {LocalDate} weekEnd
   * @param {number} reportNumber
   * @param {boolean} submitted
   * @param {boolean} approved
   * @param {boolean} rejected
   */
  constructor(weekStart, weekEnd,reportNumber, submitted, approved, rejected) {
    this.weekStart = weekStart;
    this.weekEnd = weekEnd;
    this.reportNumber = reportNumber
    this.submitted = submitted;
    this.approved = approved;
    this.rejected = rejected;
  }
};
