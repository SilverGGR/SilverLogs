export default class AuthUserDto {
  /**
   * @param {string} username
   * @param {string} firstname
   * @param {string} lastname
   * @param {string} email
   * @param {string} phone
   * @param {string} department
   * @param {string} role
   * @param profileImage
   * @param {string} profileImageType
   * @param {string} password
   */
  constructor(username, firstname, lastname, email, phone, department, role, profileImage, profileImageType, password) {
    this.username = username;
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
    this.phone = phone;
    this.department = department;
    this.role = role;
    this.profileImage = profileImage;
    this.profileImageType = profileImageType;
    this.password = password;
  }
}
