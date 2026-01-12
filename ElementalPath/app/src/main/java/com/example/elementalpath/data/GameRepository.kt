package com.example.elementalpath.data
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getAllElements(): Flow<List<ElementEntity>>
    suspend fun initData()
    suspend fun saveLog(log: GameLog)
}

class OfflineGameRepository(private val dao: GameDao) : GameRepository {
    override fun getAllElements() = dao.getAllElements()
    override suspend fun saveLog(log: GameLog) = dao.insertLog(log)
    override suspend fun initData() {
        if (dao.getCount() == 0) {
            val seeds = listOf(
                // --- Perioda 1 ---
                ElementEntity(1, "H", "Hydrogen", 1, 1),
                ElementEntity(2, "He", "Helium", 1, 18),

                // --- Perioda 2 ---
                ElementEntity(3, "Li", "Lithium", 2, 1),
                ElementEntity(4, "Be", "Beryllium", 2, 2),
                ElementEntity(5, "B", "Boron", 2, 13),
                ElementEntity(6, "C", "Carbon", 2, 14),
                ElementEntity(7, "N", "Nitrogen", 2, 15),
                ElementEntity(8, "O", "Oxygen", 2, 16),
                ElementEntity(9, "F", "Fluorine", 2, 17),
                ElementEntity(10, "Ne", "Neon", 2, 18),

                // --- Perioda 3 ---
                ElementEntity(11, "Na", "Sodium", 3, 1),
                ElementEntity(12, "Mg", "Magnesium", 3, 2),
                ElementEntity(13, "Al", "Aluminium", 3, 13),
                ElementEntity(14, "Si", "Silicon", 3, 14),
                ElementEntity(15, "P", "Phosphorus", 3, 15),
                ElementEntity(16, "S", "Sulfur", 3, 16),
                ElementEntity(17, "Cl", "Chlorine", 3, 17),
                ElementEntity(18, "Ar", "Argon", 3, 18),

                // --- Perioda 4 ---
                ElementEntity(19, "K", "Potassium", 4, 1),
                ElementEntity(20, "Ca", "Calcium", 4, 2),
                ElementEntity(21, "Sc", "Scandium", 4, 3),
                ElementEntity(22, "Ti", "Titanium", 4, 4),
                ElementEntity(23, "V", "Vanadium", 4, 5),
                ElementEntity(24, "Cr", "Chromium", 4, 6),
                ElementEntity(25, "Mn", "Manganese", 4, 7),
                ElementEntity(26, "Fe", "Iron", 4, 8),
                ElementEntity(27, "Co", "Cobalt", 4, 9),
                ElementEntity(28, "Ni", "Nickel", 4, 10),
                ElementEntity(29, "Cu", "Copper", 4, 11),
                ElementEntity(30, "Zn", "Zinc", 4, 12),
                ElementEntity(31, "Ga", "Gallium", 4, 13),
                ElementEntity(32, "Ge", "Germanium", 4, 14),
                ElementEntity(33, "As", "Arsenic", 4, 15),
                ElementEntity(34, "Se", "Selenium", 4, 16),
                ElementEntity(35, "Br", "Bromine", 4, 17),
                ElementEntity(36, "Kr", "Krypton", 4, 18),

                // --- Perioda 5 ---
                ElementEntity(37, "Rb", "Rubidium", 5, 1),
                ElementEntity(38, "Sr", "Strontium", 5, 2),
                ElementEntity(39, "Y", "Yttrium", 5, 3),
                ElementEntity(40, "Zr", "Zirconium", 5, 4),
                ElementEntity(41, "Nb", "Niobium", 5, 5),
                ElementEntity(42, "Mo", "Molybdenum", 5, 6),
                ElementEntity(43, "Tc", "Technetium", 5, 7),
                ElementEntity(44, "Ru", "Ruthenium", 5, 8),
                ElementEntity(45, "Rh", "Rhodium", 5, 9),
                ElementEntity(46, "Pd", "Palladium", 5, 10),
                ElementEntity(47, "Ag", "Silver", 5, 11),
                ElementEntity(48, "Cd", "Cadmium", 5, 12),
                ElementEntity(49, "In", "Indium", 5, 13),
                ElementEntity(50, "Sn", "Tin", 5, 14),
                ElementEntity(51, "Sb", "Antimony", 5, 15),
                ElementEntity(52, "Te", "Tellurium", 5, 16),
                ElementEntity(53, "I", "Iodine", 5, 17),
                ElementEntity(54, "Xe", "Xenon", 5, 18),

                // --- Perioda 6 (bez Lanthanoidů) ---
                ElementEntity(55, "Cs", "Caesium", 6, 1),
                ElementEntity(56, "Ba", "Barium", 6, 2),
                // Zde je mezera pro Lanthanoidy (57-71)
                ElementEntity(72, "Hf", "Hafnium", 6, 4),
                ElementEntity(73, "Ta", "Tantalum", 6, 5),
                ElementEntity(74, "W", "Tungsten", 6, 6),
                ElementEntity(75, "Re", "Rhenium", 6, 7),
                ElementEntity(76, "Os", "Osmium", 6, 8),
                ElementEntity(77, "Ir", "Iridium", 6, 9),
                ElementEntity(78, "Pt", "Platinum", 6, 10),
                ElementEntity(79, "Au", "Gold", 6, 11),
                ElementEntity(80, "Hg", "Mercury", 6, 12),
                ElementEntity(81, "Tl", "Thallium", 6, 13),
                ElementEntity(82, "Pb", "Lead", 6, 14),
                ElementEntity(83, "Bi", "Bismuth", 6, 15),
                ElementEntity(84, "Po", "Polonium", 6, 16),
                ElementEntity(85, "At", "Astatine", 6, 17),
                ElementEntity(86, "Rn", "Radon", 6, 18),

                // --- Perioda 7 (bez Aktinoidů) ---
                ElementEntity(87, "Fr", "Francium", 7, 1),
                ElementEntity(88, "Ra", "Radium", 7, 2),
                // Zde je mezera pro Aktinoidy (89-103)
                ElementEntity(104, "Rf", "Rutherfordium", 7, 4),
                ElementEntity(105, "Db", "Dubnium", 7, 5),
                ElementEntity(106, "Sg", "Seaborgium", 7, 6),
                ElementEntity(107, "Bh", "Bohrium", 7, 7),
                ElementEntity(108, "Hs", "Hassium", 7, 8),
                ElementEntity(109, "Mt", "Meitnerium", 7, 9),
                ElementEntity(110, "Ds", "Darmstadtium", 7, 10),
                ElementEntity(111, "Rg", "Roentgenium", 7, 11),
                ElementEntity(112, "Cn", "Copernicium", 7, 12),
                ElementEntity(113, "Nh", "Nihonium", 7, 13),
                ElementEntity(114, "Fl", "Flerovium", 7, 14),
                ElementEntity(115, "Mc", "Moscovium", 7, 15),
                ElementEntity(116, "Lv", "Livermorium", 7, 16),
                ElementEntity(117, "Ts", "Tennessine", 7, 17),
                ElementEntity(118, "Og", "Oganesson", 7, 18),

                // --- Lanthanoidy (Vizuálně řádek 9, sloupce 4-18) ---
                ElementEntity(57, "La", "Lanthanum", 9, 4),
                ElementEntity(58, "Ce", "Cerium", 9, 5),
                ElementEntity(59, "Pr", "Praseodymium", 9, 6),
                ElementEntity(60, "Nd", "Neodymium", 9, 7),
                ElementEntity(61, "Pm", "Promethium", 9, 8),
                ElementEntity(62, "Sm", "Samarium", 9, 9),
                ElementEntity(63, "Eu", "Europium", 9, 10),
                ElementEntity(64, "Gd", "Gadolinium", 9, 11),
                ElementEntity(65, "Tb", "Terbium", 9, 12),
                ElementEntity(66, "Dy", "Dysprosium", 9, 13),
                ElementEntity(67, "Ho", "Holmium", 9, 14),
                ElementEntity(68, "Er", "Erbium", 9, 15),
                ElementEntity(69, "Tm", "Thulium", 9, 16),
                ElementEntity(70, "Yb", "Ytterbium", 9, 17),
                ElementEntity(71, "Lu", "Lutetium", 9, 18),

                // --- Aktinoidy (Vizuálně řádek 10, sloupce 4-18) ---
                ElementEntity(89, "Ac", "Actinium", 10, 4),
                ElementEntity(90, "Th", "Thorium", 10, 5),
                ElementEntity(91, "Pa", "Protactinium", 10, 6),
                ElementEntity(92, "U", "Uranium", 10, 7),
                ElementEntity(93, "Np", "Neptunium", 10, 8),
                ElementEntity(94, "Pu", "Plutonium", 10, 9),
                ElementEntity(95, "Am", "Americium", 10, 10),
                ElementEntity(96, "Cm", "Curium", 10, 11),
                ElementEntity(97, "Bk", "Berkelium", 10, 12),
                ElementEntity(98, "Cf", "Californium", 10, 13),
                ElementEntity(99, "Es", "Einsteinium", 10, 14),
                ElementEntity(100, "Fm", "Fermium", 10, 15),
                ElementEntity(101, "Md", "Mendelevium", 10, 16),
                ElementEntity(102, "No", "Nobelium", 10, 17),
                ElementEntity(103, "Lr", "Lawrencium", 10, 18)
            )
            dao.insertElements(seeds)
        }
    }


}