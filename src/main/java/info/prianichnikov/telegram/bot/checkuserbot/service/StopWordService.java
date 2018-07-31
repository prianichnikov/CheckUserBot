package info.prianichnikov.telegram.bot.checkuserbot.service;

import java.util.*;
import java.util.regex.Pattern;

public class StopWordService {

    private final List<Pattern> obsceneWordsRU = Arrays.asList(
            Pattern.compile("[б|6]ля"),
            Pattern.compile("п[ие]([з3])д"),
            Pattern.compile("з([аa])[е|ё]б"),
            Pattern.compile("([хx])([еe])([рp])"),
            Pattern.compile("([мm])([уyu])([дd])([аa])([кkг])"),
            Pattern.compile("т([рp])([аa])([хx])([аa])[([еe])|ё|т]"),
            Pattern.compile("([еe])б([аa])[т|н]"),
            Pattern.compile("г([оo])вн[([оo])|ю|я]"),
            Pattern.compile("въ([еe])б[аaеeуyы]"),
            Pattern.compile("([сc])ц.*([уy])([кkч])([аaикьоo])"),
            Pattern.compile("(вы|н[а|a])([еeё])б"),
            Pattern.compile("([сc])([рpсc])([аa])([тt])ь"),
            Pattern.compile("([з|3])л([оo])([еeё])б"),
            Pattern.compile("([сc])([уy])ч?([кk])([аaиоo])"),
            Pattern.compile("п([еe])ди([кk])"),
            Pattern.compile("п([еe])д([рp])и([кkл])"),
            Pattern.compile("пид([аaоo])([рp])"),
            Pattern.compile("д([оo])лб([оo])([еeё])?б?"),
            Pattern.compile("д([рp])([оo])ч[ие]?т?ь?"),
            Pattern.compile("пи([сc])ь([кk])([аaоo])?"),
            Pattern.compile("пи([сc])([юя])"),
            Pattern.compile("ш([аa])л([аa])в"),
            Pattern.compile("шлю[х|ш]"),
            Pattern.compile("([хx])([уy])[й|(ли)|е|e|ё|и|я|ю]"),
            Pattern.compile("([уy])([ёеe])б[и|к|о]"),
            Pattern.compile("[e|e|ё]б[a|a|е|e|ё][л|т|н]"),
            Pattern.compile("г[а|о][в|м]н[о|ю|a|е|и]"),
            Pattern.compile("г[а|о][в|м]н[о|ю|a|е|и]")
    );

    public boolean isContainsObsceneWords(final String message) {
        return obsceneWordsRU.stream()
                .anyMatch(pattern -> pattern.matcher(message.toLowerCase()).find());
    }
}
