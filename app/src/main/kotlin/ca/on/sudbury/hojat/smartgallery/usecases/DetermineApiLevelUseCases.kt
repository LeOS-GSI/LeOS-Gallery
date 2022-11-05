package ca.on.sudbury.hojat.smartgallery.usecases

import android.os.Build

object IsMarshmallowPlusUseCase {
    operator fun invoke() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

object IsNougatPlusUseCase {
    operator fun invoke() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}

object IsOreoPlusUseCase {
    operator fun invoke() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}

object IsPiePlusUseCase {
    operator fun invoke() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
}

object IsQPlusUseCase {
    operator fun invoke() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

object IsRPlusUseCase {
    operator fun invoke() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}

object IsSPlusUseCase {
    operator fun invoke() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}