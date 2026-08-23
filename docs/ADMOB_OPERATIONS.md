# AdMob traffic-quality operations

The app-side safety controls work without Firebase. Remote switches and Analytics reporting become
active after the production Firebase configuration is installed.

## One-time Firebase and AdMob setup

1. Register Android app `com.alpware.keymapkit` in the Firebase project linked to the AdMob app.
2. Enable Google Analytics for that Firebase project.
3. Download `google-services.json` and place it at `app/google-services.json`. It is intentionally
   ignored by Git. The Gradle build applies the Google Services plugin when this file exists.
4. In AdMob, link the app to Firebase, enable impression-level ad revenue, publish the European
   regulations message, and enable Consent Mode. UMP 4 initializes after Firebase in the app.
5. Confirm the Play Console Data safety form and the public privacy-policy URL describe AdMob,
   UMP, Firebase Analytics, Remote Config, approximate country, attribution, and ad interactions.
6. Disable or conservatively configure automatic banner refresh in the AdMob ad-unit settings.
   The app caps explicit loads, but SDK-managed refresh is controlled by the AdMob console.
7. Review the Play target-audience declaration. The app does not infer age. If children are in the
   target audience, add an age-appropriate gate and send TFUA on both UMP and ad requests before
   enabling ads for those users.

## Remote Config parameters

| Parameter | Default | Immutable client boundary |
|---|---:|---:|
| `ads_banner_enabled` | `true` | kill switch; use `false` to stop |
| `ads_interstitial_enabled` | `true` | kill switch; use `false` to stop |
| `ads_interstitial_actions_required` | `7` | `5..20` |
| `ads_interstitial_cooldown_seconds` | `900` | `600..86400` |
| `ads_interstitial_min_session_age_seconds` | `120` | `90..1800` |
| `ads_interstitial_max_per_session` | `1` | `0..1` |
| `ads_interstitial_max_per_day` | `2` | `0..2` |

Production fetches use a 12-hour minimum interval. Do not configure a lower production interval.
Values outside the boundaries are clamped locally. Remote Config can therefore disable ads or make
the policy stricter but cannot exceed the hard session/day caps or lower the hard cooldown/action
floors.

## Local circuit breaker

The binary enforces limits which Remote Config cannot change:

- Banner: at most 6 explicit loads/hour and 4 retry attempts per foreground session.
- Interstitial: at most 4 explicit loads/hour and 3 retry attempts per foreground session.
- Banner: 30 impressions/hour per device before a 24-hour local suspension.
- Interstitial: 2 impressions/day per device.
- Either format: more than 2 clicks/day per device triggers a 24-hour local suspension.

The breaker emits `ad_traffic_alert` before suspending the affected format. These limits are a last
line of defense, not a substitute for reviewing AdMob invalid-traffic reports.

## Analytics dimensions and alerts

The app emits:

- `monetization_ad_event`: `ad_format`, `ad_action`, `ad_placement`, `retry_attempt`, `error_code`
- `monetization_ad_revenue`: `ad_format`, `ad_placement`, `currency`, `value`, `value_precision`
- `ad_traffic_alert`: `ad_format`, `alert_reason`
- `ad_consent_result`: `can_request_ads`, `error_code`

Firebase automatically attaches app version and provides country and acquisition-source dimensions.
Do not derive a country from device locale; Firebase's aggregate country dimension is the correct
reporting source.

In Google Analytics Admin > Custom definitions, register event-scoped dimensions for `ad_format`,
`ad_action`, `ad_placement`, `error_code`, and `alert_reason`. Then create daily custom insights with
email notification for:

1. Any `ad_traffic_alert` event count above zero.
2. An anomalous increase in `monetization_ad_event` filtered to `ad_action = clicked`.
3. An anomalous increase in `load_failed`, split by app version and ad format.
4. A material CTR shift by country or acquisition source.

App-event custom insights are daily rather than hourly. For faster or more detailed alerting, link
Analytics to BigQuery and schedule queries over `ad_traffic_alert` and impression/click ratios, then
route failures to Cloud Monitoring. Review by country, app version, first-user source/medium, ad
format, and placement. Never log typing-test text, keyboard input, selected layouts, precise location,
or a raw advertising identifier.

## Release checklist

- New developer phones are registered as AdMob test devices; every test creative shows `Test Ad`.
- Debug builds use only Google's sample ad-unit IDs.
- `google-services.json` is present in the controlled release environment.
- UMP is forced to EEA geography on a test device and both consent and privacy-options flows pass.
- Remote kill switches are tested before rollout.
- Banner and interstitial ad units remain separate for report segmentation.
- Rollout starts staged; CTR, requests, impressions, match rate, and safety alerts are reviewed daily.
