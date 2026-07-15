# Contributing

Keyboard layouts are stored as KCM files under `app/src/main/res/raw`. The provider catalog is generated from `app/src/main/res/xml/keyboard_layouts.xml`.

When adding a layout:

1. Add and validate the KCM file.
2. Add its localized label.
3. Add the layout to the source keyboard-layout XML.
4. Run the provider generator before committing.
5. Test the mapping on a physical keyboard.

Compatibility reports should include Android version, device manufacturer, keyboard connection type, layout name, and the affected key combinations.
