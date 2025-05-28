export default class ReportBadgeDto{
  /**
   * @param {LocalDate} weekStart
   * @param {LocalDate} weekEnd
   * @param {boolean} submitted
   * @param {boolean} approved
   * @param {boolean} rejected
   */
  constructor(weekStart, weekEnd, submitted, approved, rejected) {
    this.weekStart = weekStart;
    this.weekEnd = weekEnd;
    this.submitted = submitted;
    this.approved = approved;
    this.rejected = rejected;
  }
};
