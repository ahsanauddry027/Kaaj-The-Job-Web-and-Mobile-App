# Release builds are not minified (isMinifyEnabled = false), so no rules are needed yet.
# If you turn minification on, keep the kotlinx.serialization classes:
# -keepattributes *Annotation*, InnerClasses
# -keep,includedescriptorclasses class app.kaaj.android.**$$serializer { *; }
