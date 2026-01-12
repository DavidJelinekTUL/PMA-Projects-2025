package com.example.christmassapp.data

import com.example.christmassapp.model.MortalSoul

object SoulRepository {
    val souls = listOf(
        MortalSoul(1, "Senior Java Developer", 12, "Mechanickou klávesnici (modré switche)", "Píše kód bez komentářů", "Opravil bug v produkci v pátek v 17:00"),
        MortalSoul(2, "Junior Pepa", 95, "Senioritu přes noc", "Smazal produkční databázi (zapomněl WHERE)", "Nosí kafe"),
        MortalSoul(3, "Scrum Masterka Jana", 88, "Jira Premium účet", "Dělá meetingy, které mohly být e-mail", "Umí hezky lepit post-it papírky"),
        MortalSoul(4, "Linux Guru", 5, "Sponzorovat Arch Linux", "Směje se uživatelům Windows", "Napsal skript, co za něj pracuje"),
        MortalSoul(5, "Uživatel VIMu", 50, "Umět ukončit VIM", "Nemůže najít klávesu ESC", "Používá jen klávesnici, myš zahodil"),
        MortalSoul(6, "Full-Stack František", 30, "Dofouknout kruhy pod očima", "Zarovnává v CSS pomocí !important", "Seeduje Linux ISOs (a filmy)"),
        MortalSoul(7, "Crypto Bro", 99, "RTX 5090 na těžbu", "Těží Ethereum na firemním serveru", "Vysvětluje všem NFTčka"),
        MortalSoul(8, "Testerka Lenka", 20, "Nerozbitný telefon", "Vždycky najde chybu, když chceš jít domů", "Zachránila release před katastrofou"),
        MortalSoul(9, "Pythonista", 40, "Více mezer, méně závorek", "Hádá se, že Python je rychlejší než C++", "Automatizoval objednávání pizzy"),
        MortalSoul(10, "Sv. Petr (SysAdmin)", 0, "Nový cloud provider", "Restartoval vesmír, když se zasekl", "Má uptime 99.99%")
    )

    fun getSoulById(id: Int): MortalSoul? = souls.find { it.id == id }
}