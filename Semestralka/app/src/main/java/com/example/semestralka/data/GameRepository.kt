package com.example.semestralka.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class GameRepository(
    private val dao: GameDao,
    private val firestore: FirebaseFirestore
) {
    // --- Local ---
    fun getElements(): Flow<List<ElementEntity>> = dao.getAllElements()

    suspend fun initData() {
        if (dao.getCount() == 0) {
            val elements = listOf(
                // 1. Perioda
                ElementEntity(1, "H", "Hydrogen", "Vodík", 1, 1),
                ElementEntity(2, "He", "Helium", "Helium", 1, 18),

                // 2. Perioda
                ElementEntity(3, "Li", "Lithium", "Lithium", 2, 1),
                ElementEntity(4, "Be", "Beryllium", "Beryllium", 2, 2),
                ElementEntity(5, "B", "Boron", "Bor", 2, 13),
                ElementEntity(6, "C", "Carbon", "Uhlík", 2, 14),
                ElementEntity(7, "N", "Nitrogen", "Dusík", 2, 15),
                ElementEntity(8, "O", "Oxygen", "Kyslík", 2, 16),
                ElementEntity(9, "F", "Fluorine", "Fluor", 2, 17),
                ElementEntity(10, "Ne", "Neon", "Neon", 2, 18),

                // 3. Perioda
                ElementEntity(11, "Na", "Sodium", "Sodík", 3, 1),
                ElementEntity(12, "Mg", "Magnesium", "Hořčík", 3, 2),
                ElementEntity(13, "Al", "Aluminium", "Hliník", 3, 13),
                ElementEntity(14, "Si", "Silicon", "Křemík", 3, 14),
                ElementEntity(15, "P", "Phosphorus", "Fosfor", 3, 15),
                ElementEntity(16, "S", "Sulfur", "Síra", 3, 16),
                ElementEntity(17, "Cl", "Chlorine", "Chlor", 3, 17),
                ElementEntity(18, "Ar", "Argon", "Argon", 3, 18),

                // 4. Perioda
                ElementEntity(19, "K", "Potassium", "Draslík", 4, 1),
                ElementEntity(20, "Ca", "Calcium", "Vápník", 4, 2),
                ElementEntity(21, "Sc", "Scandium", "Skandium", 4, 3),
                ElementEntity(22, "Ti", "Titanium", "Titan", 4, 4),
                ElementEntity(23, "V", "Vanadium", "Vanad", 4, 5),
                ElementEntity(24, "Cr", "Chromium", "Chrom", 4, 6),
                ElementEntity(25, "Mn", "Manganese", "Mangan", 4, 7),
                ElementEntity(26, "Fe", "Iron", "Železo", 4, 8),
                ElementEntity(27, "Co", "Cobalt", "Kobalt", 4, 9),
                ElementEntity(28, "Ni", "Nickel", "Nikl", 4, 10),
                ElementEntity(29, "Cu", "Copper", "Měď", 4, 11),
                ElementEntity(30, "Zn", "Zinc", "Zinek", 4, 12),
                ElementEntity(31, "Ga", "Gallium", "Gallium", 4, 13),
                ElementEntity(32, "Ge", "Germanium", "Germanium", 4, 14),
                ElementEntity(33, "As", "Arsenic", "Arsen", 4, 15),
                ElementEntity(34, "Se", "Selenium", "Selen", 4, 16),
                ElementEntity(35, "Br", "Bromine", "Brom", 4, 17),
                ElementEntity(36, "Kr", "Krypton", "Krypton", 4, 18),

                // 5. Perioda
                ElementEntity(37, "Rb", "Rubidium", "Rubidium", 5, 1),
                ElementEntity(38, "Sr", "Strontium", "Stroncium", 5, 2),
                ElementEntity(39, "Y", "Yttrium", "Yttrium", 5, 3),
                ElementEntity(40, "Zr", "Zirconium", "Zirkonium", 5, 4),
                ElementEntity(41, "Nb", "Niobium", "Niob", 5, 5),
                ElementEntity(42, "Mo", "Molybdenum", "Molybden", 5, 6),
                ElementEntity(43, "Tc", "Technetium", "Technecium", 5, 7),
                ElementEntity(44, "Ru", "Ruthenium", "Ruthenium", 5, 8),
                ElementEntity(45, "Rh", "Rhodium", "Rhodium", 5, 9),
                ElementEntity(46, "Pd", "Palladium", "Palladium", 5, 10),
                ElementEntity(47, "Ag", "Silver", "Stříbro", 5, 11),
                ElementEntity(48, "Cd", "Cadmium", "Kadmium", 5, 12),
                ElementEntity(49, "In", "Indium", "Indium", 5, 13),
                ElementEntity(50, "Sn", "Tin", "Cín", 5, 14),
                ElementEntity(51, "Sb", "Antimony", "Antimon", 5, 15),
                ElementEntity(52, "Te", "Tellurium", "Tellur", 5, 16),
                ElementEntity(53, "I", "Iodine", "Jod", 5, 17),
                ElementEntity(54, "Xe", "Xenon", "Xenon", 5, 18),

                // 6. Perioda
                ElementEntity(55, "Cs", "Caesium", "Cesium", 6, 1),
                ElementEntity(56, "Ba", "Barium", "Baryum", 6, 2),
                // Lanthanoidy (vloženy níže)
                ElementEntity(72, "Hf", "Hafnium", "Hafnium", 6, 4),
                ElementEntity(73, "Ta", "Tantalum", "Tantal", 6, 5),
                ElementEntity(74, "W", "Tungsten", "Wolfram", 6, 6),
                ElementEntity(75, "Re", "Rhenium", "Rhenium", 6, 7),
                ElementEntity(76, "Os", "Osmium", "Osmium", 6, 8),
                ElementEntity(77, "Ir", "Iridium", "Iridium", 6, 9),
                ElementEntity(78, "Pt", "Platinum", "Platina", 6, 10),
                ElementEntity(79, "Au", "Gold", "Zlato", 6, 11),
                ElementEntity(80, "Hg", "Mercury", "Rtuť", 6, 12),
                ElementEntity(81, "Tl", "Thallium", "Thallium", 6, 13),
                ElementEntity(82, "Pb", "Lead", "Olovo", 6, 14),
                ElementEntity(83, "Bi", "Bismuth", "Bismut", 6, 15),
                ElementEntity(84, "Po", "Polonium", "Polonium", 6, 16),
                ElementEntity(85, "At", "Astatine", "Astat", 6, 17),
                ElementEntity(86, "Rn", "Radon", "Radon", 6, 18),

                // 7. Perioda
                ElementEntity(87, "Fr", "Francium", "Francium", 7, 1),
                ElementEntity(88, "Ra", "Radium", "Radium", 7, 2),
                // Aktinoidy (vloženy níže)
                ElementEntity(104, "Rf", "Rutherfordium", "Rutherfordium", 7, 4),
                ElementEntity(105, "Db", "Dubnium", "Dubnium", 7, 5),
                ElementEntity(106, "Sg", "Seaborgium", "Seaborgium", 7, 6),
                ElementEntity(107, "Bh", "Bohrium", "Bohrium", 7, 7),
                ElementEntity(108, "Hs", "Hassium", "Hassium", 7, 8),
                ElementEntity(109, "Mt", "Meitnerium", "Meitnerium", 7, 9),
                ElementEntity(110, "Ds", "Darmstadtium", "Darmstadtium", 7, 10),
                ElementEntity(111, "Rg", "Roentgenium", "Roentgenium", 7, 11),
                ElementEntity(112, "Cn", "Copernicium", "Kopernicium", 7, 12),
                ElementEntity(113, "Nh", "Nihonium", "Nihonium", 7, 13),
                ElementEntity(114, "Fl", "Flerovium", "Flerovium", 7, 14),
                ElementEntity(115, "Mc", "Moscovium", "Moscovium", 7, 15),
                ElementEntity(116, "Lv", "Livermorium", "Livermorium", 7, 16),
                ElementEntity(117, "Ts", "Tennessine", "Tennessin", 7, 17),
                ElementEntity(118, "Og", "Oganesson", "Oganesson", 7, 18)
            )
            dao.insertElements(elements)
        }
    }
    suspend fun saveScore(score: ScoreEntry) {
        try {
            firestore.collection("leaderboard").add(score).await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun getDailyLeaderboard(dateId: String): List<ScoreEntry> {
        return try {
            val snapshot = firestore.collection("leaderboard")
                .whereEqualTo("dateId", dateId)
                .orderBy("moves", Query.Direction.ASCENDING)
                .limit(10)
                .get().await()
            snapshot.toObjects(ScoreEntry::class.java)
        } catch (e: Exception) { emptyList() }
    }
}