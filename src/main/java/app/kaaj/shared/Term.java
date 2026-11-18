package app.kaaj.shared;

import java.util.UUID;

/** An occupation or a skill from the taxonomy, with its Bangla and English names. */
public record Term(UUID id, String nameBn, String nameEn) {
}
