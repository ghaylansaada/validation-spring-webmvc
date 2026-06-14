package io.ghaylan.validation.model

/**
 * Standardized enumeration of validation error codes representing distinct categories of input validation failures.
 *
 * Each enum constant maps a specific validation rule violation to a human-readable error message
 * stored in the [message] property. These messages are designed to be clear, descriptive,
 * and safe to display directly to end-users or clients.
 */
enum class ConstraintErrorCode(override val message: String) : ConstraintErrorDefinition {
	
	/**
	 * A mandatory payload parameter or header is completely absent from the incoming request.
	 * * Rejects requests failing nullability checks or missing required key declarations in the data payload.
	 */
	VALUE_MISSING("This field is required and cannot be null or missing."),
	
	/**
	 * The payload property is declared but contains no actionable data, structural elements, or meaningful characters.
	 * * Catches empty strings, blank whitespace sequences, empty collections, or empty nested objects.
	 */
	VALUE_EMPTY("The provided value cannot be empty or contain only whitespace."),
	
	/**
	 * The incoming element breaks identity uniqueness boundaries within its containing structure.
	 * * Identifies duplicate entries inside a data array or set where distinct values are strictly required.
	 */
	VALUE_DUPLICATE("This value has already been entered and must be unique."),
	
	/**
	 * The input value fails to align with any allowed options within a defined list, range, or system enumeration.
	 * * Catches out-of-bounds options or unrecognized nominal values sent to closed-choice parameters.
	 */
	VALUE_NOT_ALLOWED("The selected value is not among the permitted options."),
	
	/**
	 * The field value deviates from a secondary source parameter it must mirror identically.
	 * * Validates mirroring parameters in incoming data payloads (e.g., verifying `password` matches `confirmPassword`).
	 */
	VALUE_NOT_MATCHING_REFERENCE("This value does not match the confirmation field or target reference."),
	
	/**
	 * The field value introduces an illegal identity collision by mimicking a restricted reference element.
	 * * Enforces structural variation across separate properties (e.g., blocking an account recovery email from matching the primary email).
	 */
	VALUE_COLLIDING_WITH_REFERENCE("This value is not allowed to match the restricted reference field."),
	
	/**
	 * The data type or evaluation scheme of the incoming property cannot be mathematically compared to the targeted rule boundary.
	 * * Flags type discrepancies, unaligned sort formats, or structural parsing failures during dynamic rule checks.
	 */
	VALUE_COMPARISON_MISMATCH("The compared values must have matching data types."),
	
	/**
	 * The JSON object map contains fewer fields or populated keys than the allowed baseline.
	 * * Tracks structural under-population rules on incoming request body payloads.
	 */
	OBJECT_TOO_SMALL("The provided object does not contain enough information or fields."),
	
	/**
	 * The JSON object map contains an excessive volume of data properties or unexpected parameters.
	 * * Acts as a payload defense layer against over-posting, mass-assignment vulnerabilities, or over-populated structures.
	 */
	OBJECT_TOO_LARGE("The provided object contains too many fields, exceeding the allowed limit."),
	
	/**
	 * The array container or collection payload lacks the minimum number of item entries required by the endpoint.
	 * * Ensures composite payloads supply enough array entries to proceed with processing (e.g., checking item lists or bulk uploads).
	 */
	COLLECTION_TOO_SMALL("You must provide more items in this list to meet the minimum requirement."),
	
	/**
	 * The array container or collection payload holds a total count of elements exceeding system capacity limitations.
	 * * Safeguards internal loops, batch database inserts, and processing pipelines from memory exhaustion or timeouts.
	 */
	COLLECTION_TOO_LARGE("The list contains too many items and exceeds the maximum allowed capacity."),
	
	/**
	 * The geographic latitude coordinate breaks the physical boundaries of standard coordinate reference systems.
	 * * Requires the value to be a valid decimal degrees format strictly bounded within the range of -90.0 to 90.0 inclusive.
	 */
	GEO_LATITUDE_INVALID("The latitude coordinate must be a valid decimal number between -90 and 90 degrees."),
	
	/**
	 * The geographic longitude coordinate breaks the physical boundaries of standard coordinate reference systems.
	 * * Requires the value to be a valid decimal degrees format strictly bounded within the range of -180.0 to 180.0 inclusive.
	 */
	GEO_LONGITUDE_INVALID("The longitude coordinate must be a valid decimal number between -180 and 180 degrees."),
	
	/**
	 * The calendar day component extracted from the timestamp falls outside the allowed operational days.
	 * * Enforces scheduling rules, such as restricting appointments or system transactions to specific business days.
	 */
	DAY_OF_WEEK_NOT_ALLOWED("The requested day of the week is outside the allowed scheduling hours or days."),
	
	/**
	 * The timeline check requires a historical timestamp, but the input points to a present or future moment.
	 * * Validates facts that must have already occurred, such as birth dates, transaction histories, or logs.
	 */
	INSTANT_NOT_IN_PAST("The specified date and time must be a date in the past."),
	
	/**
	 * The timeline check requires a prospective timestamp, but the input points to a present or historical moment.
	 * * Validates events that must occur ahead of time, such as future booking reservations or scheduled tasks.
	 */
	INSTANT_NOT_IN_FUTURE("The specified date and time must be a date in the future."),
	
	/**
	 * The chronological value sits prior to the minimum allowed timestamp threshold.
	 * * Enforces lower temporal limits, such as blocking registrations before a system launch date.
	 */
	INSTANT_TOO_EARLY("The specified date/time is earlier than the allowed start date/time."),
	
	/**
	 * The chronological value sits ahead of the maximum allowed timestamp threshold.
	 * * Enforces upper temporal limits, such as restricting a scheduling horizon to a maximum time frame in advance.
	 */
	INSTANT_TOO_LATE("The specified date and time is later than the allowed cutoff date."),
	
	/**
	 * The country or territory identifier fails validation against international regional schemas.
	 * * Requires the input to strictly conform to recognized ISO 3166-1 alpha-2 or alpha-3 specifications.
	 */
	COUNTRY_CODE_INVALID("The country code provided is invalid or not recognized."),
	
	/**
	 * The financial asset or monetary unit identifier violates currency serialization standards.
	 * * Requires the value to be a valid three-letter currency code conforming strictly to ISO 4217 specifications.
	 */
	CURRENCY_CODE_INVALID("The currency code provided is invalid or not recognized."),
	
	/**
	 * The linguistic tag or dialect identifier fails standardization syntax checks.
	 * * Requires the text to match standard language classification schemas, specifically ISO 639 frameworks.
	 */
	LANGUAGE_CODE_INVALID("The language code provided is invalid or not recognized."),
	
	/**
	 * The input string or payload cannot be parsed into a valid mathematical number.
	 * * Occurs when the value contains illegal alphanumeric characters, misplaced decimal signs, or formatting syntax errors.
	 */
	NUMBER_FORMAT_INVALID("The provided numeric value or factor is invalid or infinite."),
	
	/**
	 * The value violates an explicit division or step increment constraint.
	 * * Requires the input to be an exact mathematical multiple of a defined factor.
	 */
	NUMBER_NOT_MULTIPLE("This value must be an exact mathematical multiple of the required increment factor."),
	
	/**
	 * The numeric value crosses above the allowed maximum limit.
	 * * Enforces upper business thresholds or technical boundaries to prevent arithmetic overflows.
	 */
	NUMBER_TOO_LARGE("The number entered exceeds the maximum allowed value limit."),
	
	/**
	 * The numeric value crosses below the allowed minimum limit.
	 * * Enforces lower business thresholds or negative-value restrictions (e.g., requiring positive quantities).
	 */
	NUMBER_TOO_SMALL("The number entered falls below the minimum allowed value limit."),
	
	/**
	 * The ISO country code embedded within the leading two characters of the IBAN is invalid or unsupported.
	 * * Enforces regional routing boundaries by rejecting non-existent codes or countries not serviced by the platform.
	 */
	IBAN_COUNTRY_CODE_INVALID("The country code extracted from the IBAN is invalid or unsupported."),
	
	/**
	 * The IBAN character length violates the explicit standard assigned to its specific country.
	 * * Since IBAN lengths vary globally by nation, a length mismatch indicates structural truncation or overflow.
	 */
	IBAN_LENGTH_INVALID("The IBAN does not match the required character length for its respective country."),
	
	/**
	 * The MOD-97 mathematical checksum calculation over the IBAN digits fails validation.
	 * * Directly captures data entry typos, transposed digits, or structurally fabricated bank account records.
	 */
	IBAN_CHECKSUM_INVALID("The IBAN checksum verification failed. Please check for typos or missing numbers."),
	
	/**
	 * The payment card sequence contains non-numeric symbols or fails major industry structure patterns.
	 * * Rejects entries with bad spacing, illegal characters, or length parameters outside standard payment network rules.
	 */
	CREDIT_CARD_FORMAT_INVALID("The card number contains invalid characters or has an incorrect layout configuration."),
	
	/**
	 * The credit card sequence fails the mandatory Mod-10 (Luhn algorithm) mathematical verification.
	 * * Catches accidental single-digit errors or number transpositions prior to processing transactions.
	 */
	CREDIT_CARD_CHECKSUM_INVALID("The card number verification failed. Please confirm the number is typed correctly."),
	
	/**
	 * The string input has a character count below the required minimum threshold.
	 * * Enforces structural minimum limits for textual values like user names, passwords, or comments.
	 */
	TEXT_TOO_SHORT("The text entered is too short and does not meet the minimum length requirement."),
	
	/**
	 * The string input has a character count that exceeds the allowed maximum limit.
	 * * Prevents storage overflows, database truncation errors, or payload execution vectors by restricting text length.
	 */
	TEXT_TOO_LONG("The text entered is too long and exceeds the maximum allowed character limit."),
	
	/**
	 * The input string fails to pass the evaluation of a compiled regular expression.
	 * * Assures syntax compliance for custom formats, alphanumeric constraints, or strict layouts.
	 */
	TEXT_PATTERN_MISMATCH("The input format is incorrect or does not match the required pattern."),
	
	/**
	 * The rich-text string cannot be parsed as syntactically sound HTML.
	 * * The input contains unclosed elements, broken nesting hierarchies, or malformed structural syntax.
	 */
	HTML_FORMAT_INVALID("The HTML markup is malformed, invalid, or contains unclosed tags."),
	
	/**
	 * The markup contains specific HTML tags barred by system policy.
	 * * Enforced to maintain control over visual layouts and to block high-risk elements capable of executing scripts.
	 */
	HTML_TAG_NOT_ALLOWED("The HTML markup contains elements or tags that are restricted for safety reasons."),
	
	/**
	 * The input payload fails a structural comparison before and after safety filtering.
	 * * The raw text contained an aggressive volume of dangerous code elements, resulting in complete rejection.
	 */
	HTML_SANITIZATION_MISMATCH("The HTML content failed security filtering and cannot be processed."),
	
	/**
	 * An otherwise valid HTML tag contains an unrecognized or forbidden inline attribute.
	 * * Blocks dangerous event listeners or unauthorized presentation modifications.
	 */
	HTML_ATTRIBUTE_NOT_ALLOWED("An attribute within the HTML tags is unrecognized or explicitly restricted."),
	
	/**
	 * A URI resource scheme embedded inside an HTML attribute is blocked by safety policies.
	 * * Prevents malicious script execution hidden inside anchor or source hooks (e.g., blocking `javascript:` schemes).
	 */
	HTML_PROTOCOL_NOT_ALLOWED("The URL protocol or scheme used within the HTML attributes is not permitted."),
	
	/**
	 * A string intended to define a UI theme component violates hex-color syntax rules.
	 * * The value fails to conform to standard hexadecimal CSS patterns, requiring a strict 3, 4, 6, or 8-digit color layout.
	 */
	HEX_COLOR_INVALID("The provided value is not a valid hexadecimal color code."),
	
	/**
	 * The string input cannot be parsed as a valid RFC-compliant URL.
	 * * The text contains syntax errors, illegal characters, or lacks a fundamentally recognizable URL structure.
	 */
	URL_FORMAT_INVALID("The web address or URL format provided is invalid or malformed."),
	
	/**
	 * The total character length of the URL exceeds the system's defined threshold.
	 * * Prevents buffer overflows or compatibility issues with downstream components, browsers, and proxy servers.
	 */
	URL_TOO_LONG("The web address exceeds the maximum length allowed by the system."),
	
	/**
	 * The URL lacks a verifiable host or domain component.
	 * * Syntactically broken inputs fail this validation because they cannot be resolved to a network location.
	 */
	URL_HOST_MISSING("The web address is missing a valid host or domain name destination."),
	
	/**
	 * The URL explicitly defines a port number that is restricted or blacklisted.
	 * * Enforces security boundaries by blocking requests targeting unauthorized internal network services.
	 */
	URL_PORT_NOT_ALLOWED("The web address specifies a network port connection that is restricted."),
	
	/**
	 * The URL includes query string keys or values that violate payload security boundaries.
	 * * Blocks inputs attempting query-based injections, malicious tracking arguments, or forbidden key-value pairs.
	 */
	URL_QUERY_NOT_ALLOWED("The web address contains forbidden or unsafe query parameters."),
	
	/**
	 * The URL scheme (protocol) fails to match the system's strict security profile.
	 * * Enforces encrypted `https` communication by rejecting insecure or unexpected protocols.
	 */
	URL_PROTOCOL_NOT_ALLOWED("The web address protocol is unsafe or not permitted by system policy."),
	
	/**
	 * The target path component ends with a restricted or dangerous file extension.
	 * * Prevents server-side request forgery (SSRF) or client vectors from referencing executable or internal scripting assets.
	 */
	URL_EXTENSION_NOT_ALLOWED("The web address points to a resource with a restricted or disallowed file extension."),
	
	/**
	 * The string input fails evaluation against structural email formatting standards.
	 * * Rejects syntactically invalid, malformed, or broken email addressing layouts.
	 */
	EMAIL_INVALID_FORMAT("Please enter a valid email address."),
	
	/**
	 * The phone number does not align with national or international numbering plan formatting regulations.
	 * * Catches numbers that have incorrect digit configurations, missing country dial codes, or structural syntax issues.
	 */
	PHONE_INVALID_FORMAT("The phone number provided is not in a recognized or valid format."),
	
	/**
	 * The analyzed phone number belongs to a classification category blocked by system operational policies.
	 * * Restricts premium rate numbers, VOIP assignments, or specific routing types where restricted by business logic.
	 */
	PHONE_TYPE_RESTRICTED("This type of phone number (such as premium rates or VOIP) cannot be used."),
	
	/**
	 * The regional dialing origin of the telephone input belongs to a prohibited or sanctioned territorial registry.
	 * * Rejects incoming values originating from geo-blocked regions or unsupported cross-border regions.
	 */
	PHONE_COUNTRY_RESTRICTED("Phone numbers originating from this country are not supported."),
	
	/**
	 * The text payload intended for credential generation has a length sitting under the safe operational limit.
	 * * Enforces safety baselines against short, trivial sequences exposed to low-complexity brute-forcing.
	 */
	PASSWORD_TOO_SHORT("The password is too short and does not meet the safety requirements."),
	
	/**
	 * The credential string exceeds structural safety constraints or downstream boundary caps.
	 * * Mitigates denial-of-service threats stemming from massive strings run through computational-heavy hashing algorithms.
	 */
	PASSWORD_TOO_LONG("The password exceeds the maximum allowed length threshold."),
	
	/**
	 * The credential configuration fails complex structure tests by omitting an uppercase character.
	 * * Enforces structural variation targets ensuring input sequences span across different character blocks.
	 */
	PASSWORD_UPPERCASE_MISSING("The password must include at least one uppercase letter."),
	
	/**
	 * The credential configuration fails complex structure tests by omitting a lowercase character.
	 * * Enforces structural variation targets ensuring input sequences span across different character blocks.
	 */
	PASSWORD_LOWERCASE_MISSING("The password must include at least one lowercase letter."),
	
	/**
	 * The credential configuration fails complex structure tests by omitting a numeric digit.
	 * * Enforces structural variation targets ensuring input sequences span across different character blocks.
	 */
	PASSWORD_DIGIT_MISSING("The password must include at least one numeric digit."),
	
	/**
	 * The credential configuration fails complex structure tests by omitting a special symbol or punctuation character.
	 * * Enforces structural variation targets ensuring input sequences span across different character blocks.
	 */
	PASSWORD_SPECIAL_CHARACTER_MISSING("The password must include at least one special character or symbol."),
	
	/**
	 * The candidate string contains low computational complexity or matches standard credential dictionaries.
	 * * Blocks simple sequences, dictionary words, sequential character arrays, or predictable credentials vulnerable to credential stuffing.
	 */
	PASSWORD_TOO_WEAK("The password is too predictable or easily guessed. Please choose a stronger variation."),
}