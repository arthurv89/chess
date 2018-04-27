package nl.arthurvlug.chess.engine.utils;

import nl.arthurvlug.chess.engine.ace.board.ACEBoard;
import nl.arthurvlug.chess.engine.ace.board.ACEBoardUtils;
import nl.arthurvlug.chess.engine.ace.board.InitialACEBoard;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ACEBoardUtilsTest {
	@Test
	public void testDump() {
		final ACEBoard initialACEBoard = InitialACEBoard.createInitialACEBoard();
		assertThat(ACEBoardUtils.dump(initialACEBoard)).isEqualTo("toMove=0\n" +
				"black_kings=1152921504606846976\n" +
				"white_kings=16\n" +
				"black_queens=576460752303423488\n" +
				"white_queens=8\n" +
				"white_rooks=129\n" +
				"black_rooks=-9151314442816847872\n" +
				"white_bishops=36\n" +
				"black_bishops=2594073385365405696\n" +
				"white_knights=66\n" +
				"black_knights=4755801206503243776\n" +
				"white_pawns=65280\n" +
				"black_pawns=71776119061217280\n" +
				"occupiedSquares=[65535, -281474976710656]\n" +
				"unoccupied_board=281474976645120\n" +
				"occupied_board=-281474976645121\n" +
				"enemy_and_empty_board=-65536\n" +
				"white_king_or_rook_queen_side_moved=false\n" +
				"white_king_or_rook_king_side_moved=false\n" +
				"black_king_or_rook_queen_side_moved=false\n" +
				"black_king_or_rook_king_side_moved=false\n" +
				"pieces=[4, 2, 3, 5, 6, 3, 2, 4, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 7, 7, 7, 7, 7, 7, 7, 10, 8, 9, 11, 12, 9, 8, 10]\n" +
				"fiftyMove=0\n" +
				"repeatedMove=0\n" +
				"a8Bitboard=72057594037927936\n" +
				"d8Bitboard=576460752303423488\n" +
				"f8Bitboard=2305843009213693952\n" +
				"h8Bitboard=-9223372036854775808\n" +
				"g8Bitboard=4611686018427387904\n" +
				"c8Bitboard=288230376151711744\n" +
				"e8Bitboard=1152921504606846976\n" +
				"a1Bitboard=1\n" +
				"d1Bitboard=8\n" +
				"f1Bitboard=32\n" +
				"h1Bitboard=128\n" +
				"g1Bitboard=64\n" +
				"c1Bitboard=4\n" +
				"e1Bitboard=16\n" +
				"first_row=255\n" +
				"last_row=-72057594037927936\n" +
				"d1FieldIdx=3\n" +
				"f1FieldIdx=5\n" +
				"d8FieldIdx=59\n" +
				"f8FieldIdx=61\n" +
				"zobristHash=-1072491330\n" +
				"zobristRandomTable=[[0, -1155869325, 1761283695, 892128508, 1429008869, -138487339, 26273138, 431529176, 1749940626, 155629808, -1465154083, -1242363800, 655996946], [0, -155886662, -258276172, -226796111, -270230103, 1705850753, 1492578621, 685382526, -1915244828, -382464772, 2092024379, -369526632, 684358198], [0, 1262965348, -2119636700, 498074875, -985540886, -1460749695, 673222727, 1584853918, -582126989, -1978864692, -1789481587, 181670012, -1023599386], [0, 1624365379, 600276151, -1310188465, -836540342, 21582955, -2048118856, -1615025656, -518561627, 1933673321, 20841474, -634566111, 100579776], [0, -1099578295, 609982904, 2069007352, -1956122223, -1816340580, 880097008, -1266972581, 1181244667, 323788111, -1672496605, -193570837, 1177117768], [0, -1617640095, 793310972, 45889196, 691675816, 764739731, -1973979577, 816912303, 5577367, -1359243304, 2088469028, 2093861056, 543635433], [0, -112382065, 1054099045, 1694454384, 934594003, 1855455376, 1001396483, 559242116, 885948174, -662903833, 1746079594, -215436171, -2058237879], [0, -472838325, 164612758, -1750717778, -1481018061, 514704749, -1492601190, 1663228139, 1165249391, 484561621, 982697053, -34866055, -1777049646], [0, -67333094, 887930872, 1609107348, 1990072188, 1432844988, 1903583017, -162286093, 1138833300, 1558626465, -522144145, 1248685248, 220969086], [0, -2129721489, -4377362, -1587398993, -388333581, -2114584541, 2110766901, 788299095, -1559630936, 1193731667, 1464218733, 1141557491, 501095138], [0, 1841614017, 1323248980, -1214091063, -161428053, 899764468, 741521057, 587682406, -1949531143, 2124374193, 66075398, 1541870038, 1041436542], [0, -1937234195, -1909248706, -1770317568, -905419263, -1292756720, 878903023, -854023886, 471763713, -2066794714, -2032696423, 1546424057, 1900678724], [0, 1094827726, -953841937, 966463784, -72571307, -843451650, -703021893, -1290234115, 748918191, -1290244369, -305063612, -1121163101, 387176550], [0, 700524315, -1556936681, 37625698, -1584027382, 893258567, -513861514, 1878716128, 1377758715, -1484228984, 1601478698, -384115204, 1306683493], [0, -1258235519, -1188109742, -2106711811, -55395904, 658187541, -1243071905, -823599426, 1835464617, 236746071, 599636740, -1732680838, -226515880], [0, -755996415, 531163870, -1645181299, 2069583057, 428424204, -1644345913, -2107166615, -1484702883, -1255126121, 1142045419, 272512398, -1757200775], [0, 120776076, -1754755685, 313644362, 318033418, -1374666904, 1202597766, 1219810066, -2036710432, 1112362630, 1206322533, -1535956081, 1517580277], [0, -1700877091, -645787831, 1519659436, -1723860308, 1254531672, -530674760, 665612534, 1575270834, -1874850606, 1037687982, 77321958, -748389899], [0, -1098439530, 1817770749, 1614083908, 1165195938, -1160397415, -486428995, -1976707769, -1538299695, -126179518, -587303352, -898286980, -671409943], [0, 187701662, 1965807332, -590773795, -421233404, 268385879, -1014383248, 1874472537, 1454513351, 1284737441, 361860038, -1898994079, 1122391573], [0, 1836957882, -3507139, 265866195, -1776739380, -1858462143, -1451587417, -1732216817, -137527558, -1587113999, -1920675372, -1961000002, 866345822], [0, -398224277, 1058647697, 1067251123, -1259033897, 1450500331, -1686482964, -11167100, -1249746128, -532618291, -626851445, -954721027, 929459541], [0, 1354730503, 105106996, 1937066772, 1585419807, 1992168040, 761251706, -520222466, -717672862, 1137581598, -1818009612, 562933303, 180588421], [0, -1391040485, -1221606911, -1102219083, -304501960, -1761393880, -1528955526, -754651889, -1814936637, -361268990, -1180143653, -1530246690, 35870458], [0, 474550272, -968068014, 1316333528, -512824823, -1587413744, -305399169, -1948308979, -706436080, -1093919239, 1586870742, -1911071317, -524189934], [0, 844774588, -1053786306, 677018335, 181156043, 1626172888, -916506305, 1099351221, 1036528409, 1822153810, -1868245355, 214164427, -445328248], [0, -1233720037, 473621527, -1021630684, 129144647, 1359921833, -1429909100, 1255504667, -869460080, -2031365163, 1793730412, -599159739, -267687234], [0, 1831558927, 1720379939, -1100535778, 1568976204, -1014664969, 1385853619, 2028706006, 269666578, -1944657997, 323873054, -1498322257, -606171317], [0, -2092856764, 2055897241, 563365810, 136564847, 1908926620, -1324906324, -907914391, 34332788, 176278955, 1931381996, 1150612498, -223991867], [0, -1206560760, -953696289, 1237383126, -939074297, -144938186, -14931678, 1604795124, 119942735, -980570755, -1911716897, -146908901, -943495727], [0, 1026954330, 1912038915, 559298924, 1007058536, -1477114686, -1837259957, 1361611547, -1077863473, -1474247532, -1503664046, 733841539, -428409496], [0, -1907108784, 1075014264, -342389666, -1186479521, -957816454, 1040568107, 154167351, 1616738744, 1685647855, 1788916163, 193291713, -1224783508], [0, 754634158, 1910375699, -731470602, -1709763601, -1555006228, 1302951949, -1850780484, 1950212537, 2017496657, 1936875227, 2089746414, 1974405739], [0, 280878021, -266156987, 1144251008, 984283466, 1305568859, 1597472322, -872125717, 1439110646, -1806625623, -2055585349, 1750521080, 1441639346], [0, -613634679, -828600679, 1669579554, 1139224850, 139040347, -1418261474, 592193209, 221514448, 426581700, -1375217015, 1787456779, 670972566], [0, 319379702, -1678726287, 767348987, 127273681, -1253469784, 1193048624, 2099563519, -39745800, -87719374, 659483708, -299181507, 1973582847], [0, 497307557, -1326174467, 1498989691, 979834550, 719836487, -1741046079, -605440797, 418481588, 882088310, 1695233956, 1451419769, -713831370], [0, -816516950, -2083363460, 1498863619, 228034706, -1691400224, -1367833456, 1667568774, -1362180240, 2092238194, 309397693, -120190546, -1453607696], [0, -1787808847, 1588074935, -231962434, -1346770413, 964537434, 1909540044, -492701201, 812465288, -1924959471, -1465515467, 1348422200, 3302811], [0, -1255790248, 127980219, -1465040706, 1356855360, -791260744, 668205138, 454991672, -71027115, -1713557194, -2015243461, 810932623, 2003925480], [0, 1947356367, 1373136086, -1934055846, -2051880908, 1354267700, -1305185240, 862015736, -466908554, 670671589, -1090075765, -1955138769, -1101153639], [0, 407495343, -176918059, 556960967, 101355295, 363873178, 224716089, -517965328, 1572672608, -1384719951, 1950237111, 1179333575, 1783284876], [0, 2018435351, -774458244, -132726282, 1503177082, 1172038375, 48226131, 839425785, -85966551, 1590420582, -550641736, 222275237, -1175993847], [0, -884663027, 431108934, -777636435, -1082933877, 1631499554, 462586719, -1846454483, 171282833, 1547017719, 418212277, 972625762, 1981588040], [0, -1004881644, 896710239, -235784727, 118428953, -1086956678, -1561968417, -297497515, 1022639698, -1559623510, 2034696979, -398298166, -2145158329], [0, -263974646, 403119327, -173163405, -778335632, 922601633, -713943378, 414814100, 619997866, -1973386878, -2110360130, -1294120434, -1468399157], [0, 1098293060, -1543738040, -1217656383, -851336041, 1981623657, 513849075, -745550254, 1280610598, 682317806, 1549082550, 2005774125, -415022157], [0, -2102028564, -1104102078, 244254991, -1890468143, 1580532380, -505795994, 258683754, -1859578191, 1593415088, -282842404, -1875306080, -285601659], [0, -515493990, 259673237, 308230590, 984635218, 956148769, -1522741561, -1290370921, 607203293, -828505329, -1456014593, 1226439749, -1272398797], [0, 1258348082, -491753624, -1957029395, -1280712381, 786198860, 1491782812, -268555236, 207914096, 455783233, -61080030, -232772209, -226809810], [0, 170915582, 1070670776, -873247146, -1254443527, 2001847942, -844734422, 1601212083, -795787982, 1162781971, 2068466449, -1210554645, -416530770], [0, 352548108, 2059890204, -166722709, 1022689649, 1946428446, 2066256311, 1825992121, -26056714, -412269605, -414270940, -1092392843, -1933030336], [0, 282387145, 467392952, -458538063, -582498801, -863639954, 1502296318, 1953050008, -1176314392, -1157770080, 2134074450, -1604350756, 717349776], [0, 558211038, -463882369, 1855274845, 557798296, -1418988750, -2071435277, 1420441716, -2124867562, 706943879, -1687636871, -34743917, 1051146105], [0, -1913847733, -357500755, 1633599789, 740871534, 572399653, 2054592697, 1598645530, -2064867775, 867812737, 1485797100, 493737786, -886958261], [0, -535625533, 1771421828, -2021831544, -1576035317, 691640030, 1057635040, -864130523, -1794769947, -2053742029, 1864864574, 1332330060, 1150613852], [0, -635990968, 211028294, -473194279, 118810048, -1666666006, 1245871076, 1314630002, 63334278, 689044006, -1214817202, -742996982, 1650180050], [0, 350588678, -981534604, 1543347753, -1704564050, 1576734506, -1581226270, -1467031609, 980191294, 1033276720, -2093243756, 1313051559, 2025268143], [0, -496217484, -1471103419, -1532115204, 291201029, -1706306706, -1923303523, 1486783679, 146802052, 467338531, 1475933355, 112279477, 7729693], [0, -2016354647, -373744535, -241837202, -281336196, 744708947, 711064753, 1127802618, 1377684799, 2133354676, -1709524449, -933152816, 415729660], [0, -333685507, -16912384, -1131722355, -777324526, -1795369638, 1922950422, -253996926, 408632331, 1754815150, -764603272, 2052918921, 193410986], [0, 1776007137, 1999788757, -1422168327, 1341450500, -479231670, -1194994472, 1597882240, -1835101237, -687918760, -89476473, 1864571798, -956267245], [0, -1196428289, -2056886304, -140318790, -1081354226, -1112633144, -642769642, -1851916512, -71340944, -312331741, 852492784, 137709111, 1609065465], [0, -2005533035, 1149912827, -1089051038, -1315100187, 283239732, -2065403080, 1760767169, -1157494008, 229948825, -2065077512, -259685397, -1253055356]]\n" +
				"incFiftyClock=false");
	}
}
