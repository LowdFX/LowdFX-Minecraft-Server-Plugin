package at.lowdfx.lowdfx.managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DeathMessageManager {

    private Map<String, List<String>> deathMessages;
    private final File file;
    private final Gson gson;
    private final Random random = new Random();

    public DeathMessageManager(JavaPlugin plugin) {
        // Unterordner "data" im Plugin-Ordner erstellen
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        // Die deathmessages.json im data-Unterordner speichern
        this.file = new File(dataFolder, "deathmessages.json");
        this.gson = new Gson();
        if (!file.exists()) {
            saveDefaultMessages();
        }
        loadMessages();
    }

    private void saveDefaultMessages() {
        deathMessages = Map.ofEntries(
                Map.entry("fall", List.of(
                        "<player> ist zu Tode gefallen!",
                        "<player> hat den Boden zu schnell erreicht.",
                        "Der Fall war zu hoch für <player>.",
                        "Ein fataler Fehltritt – <player> stürzte ab.",
                        "<player> fiel in die Tiefe.",
                        "<player> wurde vom Abgrund verschlungen.",
                        "<player> sprang ins Nichts.",
                        "<player>s Fall wurde legendär.",
                        "<player> konnte den Fall nicht überleben.",
                        "Die Schwerkraft war zu stark für <player>."
                )),
                Map.entry("fire", List.of(
                        "<player> ging in Flammen auf.",
                        "Das Feuer verzehrte <player>.",
                        "Die Hitze war zu stark für <player>.",
                        "Ein Funke genügte – <player> verbrannte.",
                        "<player> wurde vom Feuer verschlungen.",
                        "<player>s Asche verwehte im Wind.",
                        "Ein Inferno traf <player> unbarmherzig.",
                        "<player> erlitt ein feuriges Ende.",
                        "<player>s Leben erlosch im Feuer.",
                        "Zu heiß wurde es für <player>."
                )),
                Map.entry("default", List.of(
                        "<player> ist gestorben.",
                        "Das Schicksal traf <player> unerwartet.",
                        "<player>s Abenteuer endete abrupt.",
                        "Ein tragischer Moment – <player> starb.",
                        "Das Ende kam für <player>.",
                        "<player> verlor den letzten Atemzug.",
                        "<player> konnte den Kampf nicht gewinnen.",
                        "Der Tod fand <player>.",
                        "<player>s Geschichte endete hier.",
                        "Das Licht erlosch für <player>."
                )),
                Map.entry("contact", List.of(
                        "Ein harter Kontakt besiegte <player>.",
                        "<player> wurde durch Berührung zu Fall gebracht.",
                        "Ein unglücklicher Kontakt war <player>s Ende.",
                        "Der Kontakt mit dem Tod war unvermeidlich für <player>.",
                        "<player> stieß zu stark an etwas.",
                        "Ein fataler Kontakt traf <player>.",
                        "Die Berührung war tödlich für <player>.",
                        "<player> kam in unglücklichen Kontakt mit dem Tod.",
                        "Ein kurzer Kontakt, und <player> war vorbei.",
                        "<player> konnte den Kontakt nicht überleben."
                )),
                Map.entry("entity_attack", List.of(
                        "<player> wurde von einem Gegner überwältigt.",
                        "Ein Angriff führte zum Untergang von <player>.",
                        "<player> wurde von einem Feind besiegt.",
                        "Der Angriff war zu stark für <player>.",
                        "<player> konnte dem feindlichen Angriff nicht entkommen.",
                        "Ein Gegner brachte <player> zu Fall.",
                        "<player> wurde in einem Angriff erledigt.",
                        "Ein gezielter Schlag war das Ende für <player>.",
                        "<player>s Verteidigung versagte im Kampf.",
                        "Der Feind überwand <player>."
                )),
                Map.entry("projectile", List.of(
                        "Ein Geschoss traf <player> unfehlbar.",
                        "<player> wurde von einem Pfeil durchbohrt.",
                        "Ein Projektil fand sein Ziel – <player>.",
                        "Ein fliegender Angriff nahm <player>s Leben.",
                        "Ein Schuss traf <player> präzise.",
                        "<player> konnte dem Pfeil nicht entgehen.",
                        "Der Abschuss war tödlich für <player>.",
                        "Ein Pfeil machte <player> das Leben schwer.",
                        "Ein Geschoss führte zum Tod von <player>.",
                        "<player> wurde von einem Pfeil erwischt."
                )),
                Map.entry("suffocation", List.of(
                        "<player> wurde von der Enge erstickt.",
                        "Die Luft blieb <player> verwehrt.",
                        "<player> konnte nicht mehr atmen.",
                        "Ein Mangel an Sauerstoff besiegte <player>.",
                        "<player> erstickte an der Enge.",
                        "Die Luft wurde knapp für <player>.",
                        "<player> konnte den Erstickungsschlag nicht überleben.",
                        "Der Atem stockte bei <player>.",
                        "<player> wurde von der Enge besiegt.",
                        "Die Enge raubte <player> den Atem."
                )),
                Map.entry("melting", List.of(
                        "<player>s Körper begann zu schmelzen.",
                        "Die Kälte ließ <player> zerfließen.",
                        "<player> verwandelte sich in Flüssigkeit.",
                        "Ein schmelzender Tod ereilte <player>.",
                        "<player> verlor seine feste Form.",
                        "Die Kälte schmolz <player>s Glieder.",
                        "<player> zerfloss unter der Kälte.",
                        "Ein Schmelzprozess nahm <player> das Leben.",
                        "<player>s Form löste sich auf.",
                        "Die Kälte war zu mächtig für <player>."
                )),
                Map.entry("lava", List.of(
                        "Die Lava verzehrte <player> gnadenlos.",
                        "<player> verschwand in glühender Lava.",
                        "Die Hitze der Lava war tödlich für <player>.",
                        "<player> konnte der Lava nicht entkommen.",
                        "Die Lava nahm <player>s Leben.",
                        "<player> wurde von der glühenden Masse verschlungen.",
                        "Ein Sprung in die Lava endete für <player> tragisch.",
                        "<player> wurde von der Lava erfasst.",
                        "Die Lava führte zu <player>s Untergang.",
                        "Ein feuriger Tod in Lava traf <player>."
                )),
                Map.entry("drowning", List.of(
                        "<player> ertrank im kühlen Nass.",
                        "Das Wasser nahm <player> mit.",
                        "<player> konnte nicht über Wasser bleiben.",
                        "Ein zu hoher Wasserpegel führte zum Tod von <player>.",
                        "<player> wurde vom Wasser verschlungen.",
                        "Das Ertrinken kam schnell für <player>.",
                        "<player> fand keinen Ausweg im Wasser.",
                        "Das Wasser wurde zu <player>s Grab.",
                        "<player> erlag dem Ertrinken.",
                        "Wasser war <player>s Untergang."
                )),
                Map.entry("block_explosion", List.of(
                        "Ein explodierender Block traf <player> unvorbereitet.",
                        "<player> wurde von einer Blockexplosion getroffen.",
                        "Die Explosion eines Blocks brachte <player> zu Fall.",
                        "Ein Block explodierte und erfasste <player>.",
                        "<player> konnte der Blockexplosion nicht entkommen.",
                        "Die Kraft des Blocks zerstörte <player>.",
                        "Ein explodierender Block machte <player> das Leben schwer.",
                        "Die Explosion nahm <player>s Leben.",
                        "Ein Block explodierte und war das Ende für <player>.",
                        "Die Blockkraft erwies sich als tödlich für <player>."
                )),
                Map.entry("entity_explosion", List.of(
                        "Eine Explosion eines Feindes traf <player> hart.",
                        "<player> wurde von einer massiven Explosion erfasst.",
                        "Die Explosion eines Gegners war <player>s Ende.",
                        "Ein explosionsartiger Angriff traf <player>.",
                        "Die Kraft der Explosion überwältigte <player>.",
                        "<player> konnte der Explosion nicht entkommen.",
                        "Ein feindlicher Angriff explodierte und tötete <player>.",
                        "Die Explosion führte zu <player>s Untergang.",
                        "<player> wurde von der Explosion zermalmt.",
                        "Ein explosiver Angriff war das Ende für <player>."
                )),
                Map.entry("void", List.of(
                        "<player> fiel in die endlose Leere.",
                        "Das Nichts verschlang <player>.",
                        "<player> konnte der Leere nicht entkommen.",
                        "Die unendliche Dunkelheit war <player>s Schicksal.",
                        "<player> wurde in die Leere gezogen.",
                        "Der Fall in den Abgrund war für <player> unausweichlich.",
                        "<player> verschwand im Nichts.",
                        "Die Leere nahm <player>s Leben.",
                        "<player> stürzte in die endlose Dunkelheit.",
                        "Die Leere forderte <player>s Existenz."
                )),
                Map.entry("suicide", List.of(
                        "<player> nahm sich selbst das Leben.",
                        "<player>s eigener Entschluss endete tragisch.",
                        "<player> beging Selbstmord.",
                        "Durch eigene Hand verlor <player> sein Leben.",
                        "<player> war sich seines Schicksals bewusst und handelte danach.",
                        "<player> wählte den Weg des Selbstmords.",
                        "<player> setzte ein Ausrufezeichen ins Ende.",
                        "<player>s Leben endete durch eigene Entscheidung.",
                        "<player> brachte sich selbst um.",
                        "Ein tragischer Selbstmord traf <player>."
                )),
                Map.entry("magic", List.of(
                        "Ein mächtiger Zauber traf <player> unbarmherzig.",
                        "<player> wurde von magischen Kräften überwältigt.",
                        "Die Magie nahm <player>s Leben.",
                        "Ein Zauber führte zu <player>s Untergang.",
                        "Die magischen Kräfte waren zu stark für <player>.",
                        "<player> konnte dem Zauber nicht entkommen.",
                        "Ein magischer Angriff brachte <player> zu Fall.",
                        "Die Magie besiegte <player>.",
                        "<player>s Schicksal lag in magischen Händen.",
                        "Ein verheerender Zauber endete für <player>."
                )),
                Map.entry("wither", List.of(
                        "Der Wither traf <player> mit dunkler Macht.",
                        "<player> wurde vom Wither überwältigt.",
                        "Die zerstörerische Kraft des Withers forderte <player>s Leben.",
                        "Der Wither ließ <player> nicht entkommen.",
                        "Ein Angriff des Withers endete tragisch für <player>.",
                        "<player> wurde vom Wither zermalmt.",
                        "Die Macht des Withers war zu stark für <player>.",
                        "<player> fiel dem Wither zum Opfer.",
                        "Der dunkle Angriff des Withers war das Ende für <player>.",
                        "Mit erhöhter Gewalt traf der Wither <player>."
                )),
                Map.entry("falling_block", List.of(
                        "Ein fallender Block traf <player> unvorbereitet.",
                        "<player> wurde von einem herabstürzenden Block getroffen.",
                        "Ein Block aus der Höhe war das Ende für <player>.",
                        "<player> konnte dem fallenden Block nicht entkommen.",
                        "Der herabfallende Block traf <player> hart.",
                        "<player> wurde von einem Block überrollt.",
                        "Ein Block aus der Höhe brachte <player> zu Fall.",
                        "Die fallende Masse eines Blocks erfasste <player>.",
                        "Ein Block fiel und beendete <player>s Geschichte.",
                        "Der fallende Block war unbarmherzig gegenüber <player>."
                )),
                Map.entry("thorns", List.of(
                        "Dornen zerkratzten <player> bis zum Tod.",
                        "<player> wurde von Dornen verletzt.",
                        "Ein Stich von Dornen war tödlich für <player>.",
                        "Die Dornen forderten ihren Tribut bei <player>.",
                        "<player> konnte den Dornen nicht entkommen.",
                        "Dornen raubten <player> das Leben.",
                        "Ein heftiger Dornenstich traf <player>.",
                        "Die Dornen machten <player>s Ende unvermeidlich.",
                        "<player> wurde von Dornen überwältigt.",
                        "Die Dornen waren das letzte Hindernis für <player>."
                )),
                Map.entry("custom", List.of(
                        "Ein unheimliches Schicksal traf <player>.",
                        "<player>s Tod war von dunklen Mächten bestimmt.",
                        "Etwas Unbekanntes führte zu <player>s Ende.",
                        "Ein mysteriöses Ereignis war das letzte Kapitel von <player>.",
                        "Das Schicksal nahm eine seltsame Wendung bei <player>.",
                        "<player>s Leben endete auf unerklärliche Weise.",
                        "Ein rätselhaftes Ende ereilte <player>.",
                        "Das Unbekannte forderte <player>s Existenz.",
                        "<player> wurde von einer unbekannten Kraft besiegt.",
                        "Ein seltsames Schicksal besiegelte <player>s Ende."
                ))
        );

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(deathMessages, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            deathMessages = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRandomMessage(String deathType) {
        List<String> messages = deathMessages.get(deathType);
        if (messages == null || messages.isEmpty()) {
            messages = deathMessages.get("default");
        }
        return messages.get(random.nextInt(messages.size()));
    }

    /**
     * Liefert eine formatierte Todesnachricht. Es wird automatisch der Prefix "[💀]" hinzugefügt,
     * die gesamte Nachricht in Rot gesetzt und der Platzhalter <player> wird durch den
     * fett und weiß hervorgehobenen Spielernamen ersetzt.
     *
     * @param deathType Der Schlüssel für die Todesart (z. B. "fall", "fire", etc.)
     * @param playerName Der Name des toten Spielers
     * @return Die formatierte Todesnachricht als Legacy-String (mit §-Codes)
     */
    public Component getFormattedMessage(String deathType, String playerName) {
        String rawMessage = getRandomMessage(deathType);
        String formattedPlayerName = "<bold><dark_red>" + playerName + "</dark_red></bold>";
        String messageWithPlayer = rawMessage.replace("<player>", formattedPlayerName);
        String finalMessage = "<red><bold>[💀] </bold>" + messageWithPlayer + "</red>";
        return MiniMessage.miniMessage().deserialize(finalMessage);
    }
}
