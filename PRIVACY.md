# Privacy

KeymapKit's keyboard-layout functionality works on the device. The official Google Play build uses Google advertising, consent, measurement, and remote-configuration services. The app does not create accounts or transmit keyboard input.

The app stores only the identifiers of layouts selected by the user in private local preferences. Text entered in the layout test field is held only in screen memory and is not persisted or transmitted.

## Advertising and consent

The official Google Play build uses Google AdMob to display banner and interstitial ads.
Where required, the app uses Google's User Messaging Platform (UMP) to request and manage
advertising consent before requesting ads. A privacy-options entry is displayed in Settings
when the consent platform requires one.

Firebase Analytics records aggregate advertising operations such as ad format, load result,
impression, click, and estimated revenue. Firebase automatically makes dimensions such as country,
app version, platform, and acquisition source available in reports. KeymapKit does not send text
entered in the typing test, selected layout identifiers, email addresses, or precise location to
Analytics.

Firebase Remote Config is fetched at most once per 12-hour cache interval and is used only to
disable an ad format or make the built-in frequency policy stricter. Device-local safety counters
can temporarily suspend advertising when abnormal request, impression, or click rates are detected.

Google and its advertising/measurement partners may process identifiers, IP-derived approximate
location, advertising interactions, diagnostics, and device/app information for ad delivery,
measurement, attribution, security, and fraud prevention under their respective policies.
