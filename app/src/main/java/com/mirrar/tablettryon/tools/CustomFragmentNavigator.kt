package com.mirrar.tablettryon.tools

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator

@Navigator.Name("custom_fragment")
class CustomFragmentNavigator(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : Navigator<FragmentNavigator.Destination>() {

    override fun createDestination(): FragmentNavigator.Destination {
        return FragmentNavigator.Destination(this)
    }

    override fun navigate(
        destination: FragmentNavigator.Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        val fragmentTag = destination.id.toString()
        val transaction = fragmentManager.beginTransaction()

        val fragmentClass = destination.className
        val fragment = fragmentManager.fragmentFactory.instantiate(
            ClassLoader.getSystemClassLoader(), fragmentClass
        )
        fragment.arguments = args

        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
        transaction.replace(containerId, fragment, fragmentTag)
        transaction.addToBackStack(fragmentTag)
        transaction.commit()

        return null
    }

    override fun popBackStack(): Boolean {
        return fragmentManager.popBackStackImmediate()
    }
}
