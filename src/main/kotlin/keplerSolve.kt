import kotlin.math.*

fun keplerSolve(e: Double, M: Double, tolerance: Double = 1e-15): Double {
  /**
   * @param e - eccentricity
   * @param M - mean anomaly
   * @return - true anomaly
   */
  fun keplerStart3(e: Double, M: Double): Double {
    val eSquered = e * e
    val eCubed = eSquered * e
    val cosM = cos(M)
    return M + (-0.5*eCubed + e + (eSquered + 1.5*cosM*eCubed)*cosM)*sin(M)
  }

  fun thirdOrderIter(e: Double, M: Double, x: Double): Double {
    val cosX = cos(x)
    val eCosXMinus1 = e*cosX - 1
    val sinX = sin(x)
    val eSinX = e*sinX
    val t5 = eSinX+M-x
    val t6 = t5 / (0.5*t5*eSinX/eCosXMinus1 + eCosXMinus1)
    return t5 / ((0.5*sinX - 1/6*cosX*t6)*e*t6 + eCosXMinus1)
  }

  var dE: Double
  val MNorm = M % (2*PI)
  var E0 = keplerStart3(e, MNorm)
  var count = 0
  var E: Double
  do {
    E = E0 - thirdOrderIter(e, MNorm, E0)
    dE = abs(E - E0)
    E0 = E
    count++
    if (count >= 100) {
      throw IllegalStateException("Astounding! KeplerSolve failed to converge!")
    }
  } while (dE > tolerance)
//  console.log("KeplerSolve converged in $count iterations")
  return E
}

fun cosTheta(e: Double, cosE: Double) = (cosE - e) / (1 - e*cosE)
fun sinTheta(e: Double, cosE: Double, sinE: Double) = sqrt(1 - e*e)*sinE / (1 - e*cosE)

fun meanMotion(μ: Double, a: Double) = sqrt(μ / a.pow(3))
/**
 * @param n - mean motion
 * @param tau - time of periapsis
 */
fun M(n: Double, tau: Double, t: Double) = n * (t - tau)