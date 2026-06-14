package io.ghaylan.validation.groups

/**
 * Validation group marker interface dedicated to resource update or modification operations.
 *
 * Constraints annotated with this group are evaluated exclusively when an existing entity
 * or resource is being modified, and are safely skipped during initial creation operations.
 */
interface OnUpdate