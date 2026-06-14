package io.ghaylan.validation.groups

/**
 * Validation group marker interface dedicated to resource creation operations.
 *
 * Constraints annotated with this group are evaluated exclusively when a new entity or resource
 * is being created, and are safely skipped during modification or update operations.
 */
interface OnCreate