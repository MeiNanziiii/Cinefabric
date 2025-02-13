package ua.mei.cinefabric.util

import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import java.util.*

object HeadModels {
    val track: ItemStack = create(
        "ewogICJ0aW1lc3RhbXAiIDogMTczOTQzNTkzNTI3NiwKICAicHJvZmlsZUlkIiA6ICI2NmRmYzFmNTRlNTU0ZTZmODJjNTA5ZjM1NTJiYTkwZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJadWFyaWciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVmM2ZmM2RkMWM1Yjc3YzVjOGUyZDA1MGU2MDA0YmMxYzBkZjk5MDcxOWJjODViNWI2ODRmNjk5ZDczYWM5ZCIKICAgIH0KICB9Cn0=",
        "SN0wvIv/k/Gnj+4688+YSjvt+GjF5J9qY/6+oiuMGzaJ+w4S5+brLSVGkCC4Q7I5FcGw0WYHOedlMXC2dVy06FqALKRjH5QAQe0yzUJLK2qbLBflSJpwZPkaZCp0IJZGU3V8M61sdbD7lqzadhTxqlpHJIvKRyUD8rvDiCH+cXRoTgsoJ6/9QLSRDae7/sZofts2Yy7CHkC+dyS63Phq6XtevrTp2hzv4YLJompWQ9eDdd/cKhVUwdoaIPlll2SVcek1YGJyJub0aBfM8UNDUjs6pjmsb6+wfao4bOs/8JE6/6Wa3c5T4qvhBykkS1KzQaHwwDhpT5mUtcD0fnuTgQtSY0Z2VF0+jkKyXvgGEOuELc3OYctlG5ajwkfaNURaxmHUs80hqoeF3atT6OlSX+KDt1VhwhwbvVp12rCU6GfgKwrQHQ/SIPmYE/hnZnQ7IX8NJMbBETIEkIieJUD4qmJaWcuS4vxf4zXua0sgMMBLwgKRINRvxEqCTduL5xdUgl2U+NdNI6OoPD43PKcuhvVn4GnC2alFO5wYDK9k11bUEzXV/mWWY72bwUi0K/d0abf34IxIQsx4sXoiUDNxq1G1cuKkADnxYQzjid+Qe27+TBL03snTtZIYvN+3+ZCdIwg9ErOAk6L8HeFj5PQBWhtFolS8NRZtz7qnGDMKVQk="
    )
    val camera: ItemStack = create(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNiNjExOGIxMWUwNTY0NjZhNWM2YzVkYzVlYjkyNmY3NGE1MGQ1Njk0OWIxYzIzNGFhZWMzOTlkMWE0OGRiMyJ9fX0=",
        ""
    )

    private fun create(value: String, signature: String): ItemStack {
        return ItemStack(Items.PLAYER_HEAD)
            .with(
                DataComponentTypes.PROFILE,
                ProfileComponent(
                    Optional.empty(),
                    Optional.empty(),
                    PropertyMap().apply {
                        put("textures", Property("textures", value, signature))
                    }
                )
            )
    }
}