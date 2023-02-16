package no.nav.sosialhjelp.manueltpakoblet

import no.nav.sosialhjelp.utils.Config
import no.nav.sosialhjelp.utils.Env

private val TEST_DIGISOS_KOMMUNER =
    listOf(
        "3801", // Nytt Horten
        "0701", // Gammel Horten (1988-2020)
        "0703", // Gammel Horten (1858-1988)
        "0717", // Gammel Horten (1838-1858) (Borre)
        "0301", // Oslo
        "1201", // Mock-data Oslo
        "4627", // Nytt Askøy
        "3024", // Nytt Bærum
        "5001", // Trondheim
        "1103", // Stavanger
        "3403", // Nytt Hamar
        "3436", // Nytt Nord-Fron
        "3438", // Nytt Sør-Fron
        "3439", // Nytt Ringebu
        "1514" // Nytt Sande i Møre og Romsdal
        )
private val PROD_DIGISOS_KOMMUNER =
    listOf(
        "0301",
        "5001",
        "1103",
        "1119",
        "1124",
        "1122",
        "1866",
        "1133",
        "1135",
        "1130",
        "1134",
        "1824",
        "1127",
        "1144",
        "4624",
        "1804",
        "5041",
        "5053",
        "1517",
        "1516",
        "1514",
        "1833",
        "1149",
        "1145",
        "1515",
        "1511",
        "1106",
        "1151",
        "5031",
        "1146",
        "5037",
        "1505",
        "1554",
        "1557",
        "5406",
        "4205",
        "1579", // Hustadvika
        "4649", // Stad
        "4602", // Kinn
        "3801", // Nytt Horten
        "4627", // Nytt Askøy
        "3807", // Nytt Skien
        "1108", // Nytt Sandnes
        "3805", // Nytt Larvik
        "3403", // Nytt Hamar
        "3443", // Nytt Vestre Toten
        "3049", // Nytt Lier
        "3048", // Nytt Øvre Eiker
        "3053", // Nytt Jevnaker
        "4622", // Nytt Kvam
        "5422", // Nytt Balsfjord
        "5425", // Nytt Storfjord
        "3026", // Nytt Aurskog-Høland
        "4617", // Nytt Kvinnherad
        "3814", // Nytt Kragerø
        "3436", // Nytt Nord-Fron
        "3438", // Nytt Sør-Fron
        "3439", // Nytt Ringebu
        "3411", // Nytt Ringsaker
        "3419", // Nytt Våler i innlandet
        "4227", // Nytt Kvinesdal
        "3004", // Nytt Fredrikstad
        "3007", // Nytt Ringerike
        "3414", // Nytt Nord-Odal
        "4621", // Nytt Voss
        "5405", // Nytt Vadsø
        "3028", // Nytt Enebakk
        "3434", // Nytt Lom
        "3437", // Nytt Sel
        "3433", // Nytt Skjåk
        "4613", // Nytt Bømlo
        "3435", // Nytt Vågå
        "3407", // Nytt Gjøvik
        "4203", // Nytt Arendal
        "5402", // Nytt Harstad
        "3431", // Nytt Dovre
        "3432", // Nytt Lesja
        "5412", // Nytt Tjeldsund
        "3413", // Nytt Stange
        "5006", // Nytt Steinkjer
        "3019", // Nytt Vestby
        "4615", // Nytt Fitjar
        "3003", // Nytt Sarpsborg
        "4614", // Nytt Stord
        "3819", // Nytt Hjartdal
        "3808", // Nytt Notodden
        "3051", // Nytt Rollag
        "3050", // Nytt Flesberg
        "3052", // Nytt Nore og Uvdal
        "3006", // Nytt Kongsberg
        "3818", // Nytt Tinn
        "3420", // Nytt Elverum
        "3035", // Nytt Eidsvoll
        "3037", // Nytt Hurdal
        "3417", // Nytt Grue
        "3016", // Nytt Rakkestad
        "3034", // Nytt Nes (Akershus)
        "3032", // Nytt Gjerdrum
        "3001", // Nytt Halden
        "3033", // Nytt Ullensaker
        "3036", // Nytt Nannestad
        "3447", // Nytt Søndre Land
        "3024", // Nytt Bærum
        "4638", // Nytt Høyanger
        "3401", // Nytt Kongsvinger
        "3415", // Nytt Sør-Odal
        "3022", // Nytt Frogn
        "3029", // Nytt Lørenskog
        "3416", // Nytt Eidskog
        "4648", // Nytt Bremager
        "4650", // Nytt Gloppen
        "4651", // Nytt Stryn
        "5428", // Nytt Nordreisa
        "5427", // Nytt Skjervøy
        "3418", // Nytt Åsnes
        "3017", // Nytt Råde
        "3811", // Færder
        "4207", // Flekkefjord
        "4225", // Lyngdal
        "4206", // Farsund
        "4228", // Sirdal
        "3803", // Tønsberg
        "3802", // Holmestrand
        "1507", // Ålesund
        "1578", // Fjord
        "1532", // Giske
        "5417", // Salangen
        "5415", // Lavangen
        "5420", // Dyrøy
        "3451", // Nord-Aurdal
        "3449", // Sør-Aurdal
        "3450", // Etnedal
        "3452", // Vestre Slidre
        "3453", // Øystre Slidre
        "3454", // Vang
        "3448", // Nordre Land
        "3815", // Drangedal
        "5057", // Ørland
        "3030", // Lillestrøm
        "5441", // Deatnu/Tana
        "3020", // Nordre Follo
        "4626", // Øygarden
        "4222", // Bykle
        "4221", // Valle
        "4224", // Åseral
        "4223", // Vennesla
        "4218", // Iveland
        "3804", // Sandefjord
        "1560", // Tingvoll
        "1566", // Surnadal
        "4211", // Gjerstad
        "1563", // Sunndal
        "4647", // Sunnfjord
        "4220", // Bygland
        "5054", // Indre Fosen
        "1573", // Smøla
        "1576", // Aure
        "5032", // Selbu
        "5033", // Tydal
        "5034", // Meråker
        "5035", // Stjørdal
        "5036", // Frosta
        "3027", // Rælingen
        "1853", // Evenes
        "1806", // Narvik
        "5414", // Gratangen
        "5421", // Senja
        "3822", // Nissedal
        "3821", // Kvitseid
        "3824", // Tokke
        "3825", // Vinje
        "3823", // Fyresdal
        "3820", // Seljord
        "4213", // Tvedestrand
        "1870", // Sortland
        "3442", // Østre Toten
        "4212", // Vegårshei
        "3041", // Gol
        "3042", // Hemsedal
        "3039", // Flå
        "3040", // Nesbyen
        "3043", // Ål
        "3044", // Hol
        "5020", // Osen
        "5058", // Åfjord
        "5056", // Hitra
        "5014", // Frøya
        "5403", // Alta
        "3014", // Indre Østfold
        "4214", // Froland
        "4217", // Åmli
        "1506", // Molde
        "1120", // Klepp
        "1121", // Time
        "4201", // Risør
        "4202", // Grimstad
        "5426" // Gáivuotna/Kåfjord
        )

fun getManuelleKommuner() =
    if (Config.env == Env.PROD) PROD_DIGISOS_KOMMUNER else TEST_DIGISOS_KOMMUNER

fun getManuellKommune(kommunenummer: String) =
    if (Config.env == Env.PROD) PROD_DIGISOS_KOMMUNER.find { it == kommunenummer }
    else TEST_DIGISOS_KOMMUNER.find { it == kommunenummer }
