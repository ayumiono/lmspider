var sinaSSOEncoder = sinaSSOEncoder || {};
(function(){
	var az;
	var al = 244837814094590;
	var ab = ((al & 16777215) == 15715070);
	function av(z, t, aD) {
		if (z != null) {
			if ("number" == typeof z) {
				this.fromNumber(z, t, aD)
			} else {
				if (t == null && "string" != typeof z) {
					this.fromString(z, 256)
				} else {
					this.fromString(z, t)
				}
			}
		}
	}
	function k() {
		return new av(null)
	}
	function b(aF, t, z, aE, aH, aG) {
		while (--aG >= 0) {
			var aD = t * this[aF++] + z[aE] + aH;
			aH = Math.floor(aD / 67108864);
			z[aE++] = aD & 67108863
		}
		return aH
	}
	function aB(aF, aK, aL, aE, aI, t) {
		var aH = aK & 32767, aJ = aK >> 15;
		while (--t >= 0) {
			var aD = this[aF] & 32767;
			var aG = this[aF++] >> 15;
			var z = aJ * aD + aG * aH;
			aD = aH * aD + ((z & 32767) << 15) + aL[aE]
					+ (aI & 1073741823);
			aI = (aD >>> 30) + (z >>> 15) + aJ * aG
					+ (aI >>> 30);
			aL[aE++] = aD & 1073741823
		}
		return aI
	}
	function aA(aF, aK, aL, aE, aI, t) {
		var aH = aK & 16383, aJ = aK >> 14;
		while (--t >= 0) {
			var aD = this[aF] & 16383;
			var aG = this[aF++] >> 14;
			var z = aJ * aD + aG * aH;
			aD = aH * aD + ((z & 16383) << 14) + aL[aE] + aI;
			aI = (aD >> 28) + (z >> 14) + aJ * aG;
			aL[aE++] = aD & 268435455
		}
		return aI
	}
	av.prototype.am = aB;
	az = 30
//	if (ab&& (navigator.appName == "Microsoft Internet Explorer")) {
//		av.prototype.am = aB;
//		az = 30
//	} else {
//		if (ab && (navigator.appName != "Netscape")) {
//			av.prototype.am = b;
//			az = 26
//		} else {
//			av.prototype.am = aA;
//			az = 28
//		}
//	}
	av.prototype.DB = az;
	av.prototype.DM = ((1 << az) - 1);
	av.prototype.DV = (1 << az);
	var ac = 52;
	av.prototype.FV = Math.pow(2, ac);
	av.prototype.F1 = ac - az;
	av.prototype.F2 = 2 * az - ac;
	var ag = "0123456789abcdefghijklmnopqrstuvwxyz";
	var aj = new Array();
	var at, y;
	at = "0".charCodeAt(0);
	for (y = 0; y <= 9; ++y) {
		aj[at++] = y
	}
	at = "a".charCodeAt(0);
	for (y = 10; y < 36; ++y) {
		aj[at++] = y
	}
	at = "A".charCodeAt(0);
	for (y = 10; y < 36; ++y) {
		aj[at++] = y
	}
	function aC(t) {
		return ag.charAt(t)
	}
	function D(z, t) {
		var aD = aj[z.charCodeAt(t)];
		return (aD == null) ? -1 : aD
	}
	function aa(z) {
		for ( var t = this.t - 1; t >= 0; --t) {
			z[t] = this[t]
		}
		z.t = this.t;
		z.s = this.s
	}
	function q(t) {
		this.t = 1;
		this.s = (t < 0) ? -1 : 0;
		if (t > 0) {
			this[0] = t
		} else {
			if (t < -1) {
				this[0] = t + DV
			} else {
				this.t = 0
			}
		}
	}
	function e(t) {
		var z = k();
		z.fromInt(t);
		return z
	}
	function A(aH, z) {
		var aE;
		if (z == 16) {
			aE = 4
		} else {
			if (z == 8) {
				aE = 3
			} else {
				if (z == 256) {
					aE = 8
				} else {
					if (z == 2) {
						aE = 1
					} else {
						if (z == 32) {
							aE = 5
						} else {
							if (z == 4) {
								aE = 2
							} else {
								this.fromRadix(aH, z);
								return
							}
						}
					}
				}
			}
		}
		this.t = 0;
		this.s = 0;
		var aG = aH.length, aD = false, aF = 0;
		while (--aG >= 0) {
			var t = (aE == 8) ? aH[aG] & 255 : D(aH, aG);
			if (t < 0) {
				if (aH.charAt(aG) == "-") {
					aD = true
				}
				continue
			}
			aD = false;
			if (aF == 0) {
				this[this.t++] = t
			} else {
				if (aF + aE > this.DB) {
					this[this.t - 1] |= (t & ((1 << (this.DB - aF)) - 1)) << aF;
					this[this.t++] = (t >> (this.DB - aF))
				} else {
					this[this.t - 1] |= t << aF
				}
			}
			aF += aE;
			if (aF >= this.DB) {
				aF -= this.DB
			}
		}
		if (aE == 8 && (aH[0] & 128) != 0) {
			this.s = -1;
			if (aF > 0) {
				this[this.t - 1] |= ((1 << (this.DB - aF)) - 1) << aF
			}
		}
		this.clamp();
		if (aD) {
			av.ZERO.subTo(this, this)
		}
	}
	function R() {
		var t = this.s & this.DM;
		while (this.t > 0 && this[this.t - 1] == t) {
			--this.t
		}
	}
	function u(z) {
		if (this.s < 0) {
			return "-" + this.negate().toString(z)
		}
		var aD;
		if (z == 16) {
			aD = 4
		} else {
			if (z == 8) {
				aD = 3
			} else {
				if (z == 2) {
					aD = 1
				} else {
					if (z == 32) {
						aD = 5
					} else {
						if (z == 4) {
							aD = 2
						} else {
							return this.toRadix(z)
						}
					}
				}
			}
		}
		var aF = (1 << aD) - 1, aI, t = false, aG = "", aE = this.t;
		var aH = this.DB - (aE * this.DB) % aD;
		if (aE-- > 0) {
			if (aH < this.DB && (aI = this[aE] >> aH) > 0) {
				t = true;
				aG = aC(aI)
			}
			while (aE >= 0) {
				if (aH < aD) {
					aI = (this[aE] & ((1 << aH) - 1)) << (aD - aH);
					aI |= this[--aE] >> (aH += this.DB - aD)
				} else {
					aI = (this[aE] >> (aH -= aD)) & aF;
					if (aH <= 0) {
						aH += this.DB;
						--aE
					}
				}
				if (aI > 0) {
					t = true
				}
				if (t) {
					aG += aC(aI)
				}
			}
		}
		return t ? aG : "0"
	}
	function U() {
		var t = k();
		av.ZERO.subTo(this, t);
		return t
	}
	function ao() {
		return (this.s < 0) ? this.negate() : this
	}
	function J(t) {
		var aD = this.s - t.s;
		if (aD != 0) {
			return aD
		}
		var z = this.t;
		aD = z - t.t;
		if (aD != 0) {
			return aD
		}
		while (--z >= 0) {
			if ((aD = this[z] - t[z]) != 0) {
				return aD
			}
		}
		return 0
	}
	function m(z) {
		var aE = 1, aD;
		if ((aD = z >>> 16) != 0) {
			z = aD;
			aE += 16
		}
		if ((aD = z >> 8) != 0) {
			z = aD;
			aE += 8
		}
		if ((aD = z >> 4) != 0) {
			z = aD;
			aE += 4
		}
		if ((aD = z >> 2) != 0) {
			z = aD;
			aE += 2
		}
		if ((aD = z >> 1) != 0) {
			z = aD;
			aE += 1
		}
		return aE
	}
	function x() {
		if (this.t <= 0) {
			return 0
		}
		return this.DB * (this.t - 1)
				+ m(this[this.t - 1] ^ (this.s & this.DM))
	}
	function au(aD, z) {
		var t;
		for (t = this.t - 1; t >= 0; --t) {
			z[t + aD] = this[t]
		}
		for (t = aD - 1; t >= 0; --t) {
			z[t] = 0
		}
		z.t = this.t + aD;
		z.s = this.s
	}
	function Z(aD, z) {
		for ( var t = aD; t < this.t; ++t) {
			z[t - aD] = this[t]
		}
		z.t = Math.max(this.t - aD, 0);
		z.s = this.s
	}
	function w(aI, aE) {
		var z = aI % this.DB;
		var t = this.DB - z;
		var aG = (1 << t) - 1;
		var aF = Math.floor(aI / this.DB), aH = (this.s << z)
				& this.DM, aD;
		for (aD = this.t - 1; aD >= 0; --aD) {
			aE[aD + aF + 1] = (this[aD] >> t) | aH;
			aH = (this[aD] & aG) << z
		}
		for (aD = aF - 1; aD >= 0; --aD) {
			aE[aD] = 0
		}
		aE[aF] = aH;
		aE.t = this.t + aF + 1;
		aE.s = this.s;
		aE.clamp()
	}
	function o(aH, aE) {
		aE.s = this.s;
		var aF = Math.floor(aH / this.DB);
		if (aF >= this.t) {
			aE.t = 0;
			return
		}
		var z = aH % this.DB;
		var t = this.DB - z;
		var aG = (1 << z) - 1;
		aE[0] = this[aF] >> z;
		for ( var aD = aF + 1; aD < this.t; ++aD) {
			aE[aD - aF - 1] |= (this[aD] & aG) << t;
			aE[aD - aF] = this[aD] >> z
		}
		if (z > 0) {
			aE[this.t - aF - 1] |= (this.s & aG) << t
		}
		aE.t = this.t - aF;
		aE.clamp()
	}
	function ad(z, aE) {
		var aD = 0, aF = 0, t = Math.min(z.t, this.t);
		while (aD < t) {
			aF += this[aD] - z[aD];
			aE[aD++] = aF & this.DM;
			aF >>= this.DB
		}
		if (z.t < this.t) {
			aF -= z.s;
			while (aD < this.t) {
				aF += this[aD];
				aE[aD++] = aF & this.DM;
				aF >>= this.DB
			}
			aF += this.s
		} else {
			aF += this.s;
			while (aD < z.t) {
				aF -= z[aD];
				aE[aD++] = aF & this.DM;
				aF >>= this.DB
			}
			aF -= z.s
		}
		aE.s = (aF < 0) ? -1 : 0;
		if (aF < -1) {
			aE[aD++] = this.DV + aF
		} else {
			if (aF > 0) {
				aE[aD++] = aF
			}
		}
		aE.t = aD;
		aE.clamp()
	}
	function G(z, aE) {
		var t = this.abs(), aF = z.abs();
		var aD = t.t;
		aE.t = aD + aF.t;
		while (--aD >= 0) {
			aE[aD] = 0
		}
		for (aD = 0; aD < aF.t; ++aD) {
			aE[aD + t.t] = t.am(0, aF[aD], aE, aD, 0, t.t)
		}
		aE.s = 0;
		aE.clamp();
		if (this.s != z.s) {
			av.ZERO.subTo(aE, aE)
		}
	}
	function T(aD) {
		var t = this.abs();
		var z = aD.t = 2 * t.t;
		while (--z >= 0) {
			aD[z] = 0
		}
		for (z = 0; z < t.t - 1; ++z) {
			var aE = t.am(z, t[z], aD, 2 * z, 0, 1);
			if ((aD[z + t.t] += t.am(z + 1, 2 * t[z], aD,
					2 * z + 1, aE, t.t - z - 1)) >= t.DV) {
				aD[z + t.t] -= t.DV;
				aD[z + t.t + 1] = 1
			}
		}
		if (aD.t > 0) {
			aD[aD.t - 1] += t.am(z, t[z], aD, 2 * z, 0, 1)
		}
		aD.s = 0;
		aD.clamp()
	}
	function H(aL, aI, aH) {
		var aR = aL.abs();
		if (aR.t <= 0) {
			return
		}
		var aJ = this.abs();
		if (aJ.t < aR.t) {
			if (aI != null) {
				aI.fromInt(0)
			}
			if (aH != null) {
				this.copyTo(aH)
			}
			return
		}
		if (aH == null) {
			aH = k()
		}
		var aF = k(), z = this.s, aK = aL.s;
		var aQ = this.DB - m(aR[aR.t - 1]);
		if (aQ > 0) {
			aR.lShiftTo(aQ, aF);
			aJ.lShiftTo(aQ, aH)
		} else {
			aR.copyTo(aF);
			aJ.copyTo(aH)
		}
		var aN = aF.t;
		var aD = aF[aN - 1];
		if (aD == 0) {
			return
		}
		var aM = aD * (1 << this.F1)
				+ ((aN > 1) ? aF[aN - 2] >> this.F2 : 0);
		var aU = this.FV / aM, aT = (1 << this.F1) / aM, aS = 1 << this.F2;
		var aP = aH.t, aO = aP - aN, aG = (aI == null) ? k()
				: aI;
		aF.dlShiftTo(aO, aG);
		if (aH.compareTo(aG) >= 0) {
			aH[aH.t++] = 1;
			aH.subTo(aG, aH)
		}
		av.ONE.dlShiftTo(aN, aG);
		aG.subTo(aF, aF);
		while (aF.t < aN) {
			aF[aF.t++] = 0
		}
		while (--aO >= 0) {
			var aE = (aH[--aP] == aD) ? this.DM
					: Math.floor(aH[aP] * aU
							+ (aH[aP - 1] + aS) * aT);
			if ((aH[aP] += aF.am(0, aE, aH, aO, 0, aN)) < aE) {
				aF.dlShiftTo(aO, aG);
				aH.subTo(aG, aH);
				while (aH[aP] < --aE) {
					aH.subTo(aG, aH)
				}
			}
		}
		if (aI != null) {
			aH.drShiftTo(aN, aI);
			if (z != aK) {
				av.ZERO.subTo(aI, aI)
			}
		}
		aH.t = aN;
		aH.clamp();
		if (aQ > 0) {
			aH.rShiftTo(aQ, aH)
		}
		if (z < 0) {
			av.ZERO.subTo(aH, aH)
		}
	}
	function Q(t) {
		var z = k();
		this.abs().divRemTo(t, null, z);
		if (this.s < 0 && z.compareTo(av.ZERO) > 0) {
			t.subTo(z, z)
		}
		return z
	}
	function N(t) {
		this.m = t
	}
	function X(t) {
		if (t.s < 0 || t.compareTo(this.m) >= 0) {
			return t.mod(this.m)
		} else {
			return t
		}
	}
	function an(t) {
		return t
	}
	function M(t) {
		t.divRemTo(this.m, null, t)
	}
	function K(t, aD, z) {
		t.multiplyTo(aD, z);
		this.reduce(z)
	}
	function ax(t, z) {
		t.squareTo(z);
		this.reduce(z)
	}
	N.prototype.convert = X;
	N.prototype.revert = an;
	N.prototype.reduce = M;
	N.prototype.mulTo = K;
	N.prototype.sqrTo = ax;
	function E() {
		if (this.t < 1) {
			return 0
		}
		var t = this[0];
		if ((t & 1) == 0) {
			return 0
		}
		var z = t & 3;
		z = (z * (2 - (t & 15) * z)) & 15;
		z = (z * (2 - (t & 255) * z)) & 255;
		z = (z * (2 - (((t & 65535) * z) & 65535))) & 65535;
		z = (z * (2 - t * z % this.DV)) % this.DV;
		return (z > 0) ? this.DV - z : -z
	}
	function h(t) {
		this.m = t;
		this.mp = t.invDigit();
		this.mpl = this.mp & 32767;
		this.mph = this.mp >> 15;
		this.um = (1 << (t.DB - 15)) - 1;
		this.mt2 = 2 * t.t
	}
	function am(t) {
		var z = k();
		t.abs().dlShiftTo(this.m.t, z);
		z.divRemTo(this.m, null, z);
		if (t.s < 0 && z.compareTo(av.ZERO) > 0) {
			this.m.subTo(z, z)
		}
		return z
	}
	function aw(t) {
		var z = k();
		t.copyTo(z);
		this.reduce(z);
		return z
	}
	function S(t) {
		while (t.t <= this.mt2) {
			t[t.t++] = 0
		}
		for ( var aD = 0; aD < this.m.t; ++aD) {
			var z = t[aD] & 32767;
			var aE = (z * this.mpl + (((z * this.mph + (t[aD] >> 15)
					* this.mpl) & this.um) << 15))
					& t.DM;
			z = aD + this.m.t;
			t[z] += this.m.am(0, aE, t, aD, 0, this.m.t);
			while (t[z] >= t.DV) {
				t[z] -= t.DV;
				t[++z]++
			}
		}
		t.clamp();
		t.drShiftTo(this.m.t, t);
		if (t.compareTo(this.m) >= 0) {
			t.subTo(this.m, t)
		}
	}
	function ap(t, z) {
		t.squareTo(z);
		this.reduce(z)
	}
	function C(t, aD, z) {
		t.multiplyTo(aD, z);
		this.reduce(z)
	}
	h.prototype.convert = am;
	h.prototype.revert = aw;
	h.prototype.reduce = S;
	h.prototype.mulTo = C;
	h.prototype.sqrTo = ap;
	function l() {
		return ((this.t > 0) ? (this[0] & 1) : this.s) == 0
	}
	function B(aI, aJ) {
		if (aI > 4294967295 || aI < 1) {
			return av.ONE
		}
		var aH = k(), aD = k(), aG = aJ.convert(this), aF = m(aI) - 1;
		aG.copyTo(aH);
		while (--aF >= 0) {
			aJ.sqrTo(aH, aD);
			if ((aI & (1 << aF)) > 0) {
				aJ.mulTo(aD, aG, aH)
			} else {
				var aE = aH;
				aH = aD;
				aD = aE
			}
		}
		return aJ.revert(aH)
	}
	function aq(aD, t) {
		var aE;
		if (aD < 256 || t.isEven()) {
			aE = new N(t)
		} else {
			aE = new h(t)
		}
		return this.exp(aD, aE)
	}
	av.prototype.copyTo = aa;
	av.prototype.fromInt = q;
	av.prototype.fromString = A;
	av.prototype.clamp = R;
	av.prototype.dlShiftTo = au;
	av.prototype.drShiftTo = Z;
	av.prototype.lShiftTo = w;
	av.prototype.rShiftTo = o;
	av.prototype.subTo = ad;
	av.prototype.multiplyTo = G;
	av.prototype.squareTo = T;
	av.prototype.divRemTo = H;
	av.prototype.invDigit = E;
	av.prototype.isEven = l;
	av.prototype.exp = B;
	av.prototype.toString = u;
	av.prototype.negate = U;
	av.prototype.abs = ao;
	av.prototype.compareTo = J;
	av.prototype.bitLength = x;
	av.prototype.mod = Q;
	av.prototype.modPowInt = aq;
	av.ZERO = e(0);
	av.ONE = e(1);
	function n() {
		this.i = 0;
		this.j = 0;
		this.S = new Array()
	}
	function g(aF) {
		var aE, z, aD;
		for (aE = 0; aE < 256; ++aE) {
			this.S[aE] = aE
		}
		z = 0;
		for (aE = 0; aE < 256; ++aE) {
			z = (z + this.S[aE] + aF[aE % aF.length]) & 255;
			aD = this.S[aE];
			this.S[aE] = this.S[z];
			this.S[z] = aD
		}
		this.i = 0;
		this.j = 0
	}
	function a() {
		var z;
		this.i = (this.i + 1) & 255;
		this.j = (this.j + this.S[this.i]) & 255;
		z = this.S[this.i];
		this.S[this.i] = this.S[this.j];
		this.S[this.j] = z;
		return this.S[(z + this.S[this.i]) & 255]
	}
	n.prototype.init = g;
	n.prototype.next = a;
	function ar() {
		return new n()
	}
	var P = 256;
	var p;
	var W;
	var ae;
	function f(t) {
		W[ae++] ^= t & 255;
		W[ae++] ^= (t >> 8) & 255;
		W[ae++] ^= (t >> 16) & 255;
		W[ae++] ^= (t >> 24) & 255;
		if (ae >= P) {
			ae -= P
		}
	}
	function V() {
		f(new Date().getTime())
	}
	if (W == null) {
		W = new Array();
		ae = 0;
		var L;
//		if (navigator.appName == "Netscape"	&& navigator.appVersion < "5" && window.crypto) {
//			var I = window.crypto.random(32);
//			for (L = 0; L < I.length; ++L) {
//				W[ae++] = I.charCodeAt(L) & 255
//			}
//		}
		while (ae < P) {
			L = Math.floor(65536 * Math.random());
			W[ae++] = L >>> 8;
			W[ae++] = L & 255
		}
		ae = 0;
		V()
	}
	function F() {
		if (p == null) {
			V();
			p = ar();
			p.init(W);
			for (ae = 0; ae < W.length; ++ae) {
				W[ae] = 0
			}
			ae = 0
		}
		return p.next()
	}
	function ay(z) {
		var t;
		for (t = 0; t < z.length; ++t) {
			z[t] = F()
		}
	}
	function af() {
	}
	af.prototype.nextBytes = ay;
	function j(z, t) {
		return new av(z, t)
	}
	function ak(aD, aE) {
		var t = "";
		var z = 0;
		while (z + aE < aD.length) {
			t += aD.substring(z, z + aE) + "\n";
			z += aE
		}
		return t + aD.substring(z, aD.length)
	}
	function v(t) {
		if (t < 16) {
			return "0" + t.toString(16)
		} else {
			return t.toString(16)
		}
	}
	function ah(aE, aH) {
		if (aH < aE.length + 11) {
			alert("Message too long for RSA");
			return null
		}
		var aG = new Array();
		var aD = aE.length - 1;
		while (aD >= 0 && aH > 0) {
			var aF = aE.charCodeAt(aD--);
			if (aF < 128) {
				aG[--aH] = aF
			} else {
				if ((aF > 127) && (aF < 2048)) {
					aG[--aH] = (aF & 63) | 128;
					aG[--aH] = (aF >> 6) | 192
				} else {
					aG[--aH] = (aF & 63) | 128;
					aG[--aH] = ((aF >> 6) & 63) | 128;
					aG[--aH] = (aF >> 12) | 224
				}
			}
		}
		aG[--aH] = 0;
		var z = new af();
		var t = new Array();
		while (aH > 2) {
			t[0] = 0;
			while (t[0] == 0) {
				z.nextBytes(t)
			}
			aG[--aH] = t[0]
		}
		aG[--aH] = 2;
		aG[--aH] = 0;
		return new av(aG)
	}
	function RSAKey() {
		this.n = null;
		this.e = 0;
		this.d = null;
		this.p = null;
		this.q = null;
		this.dmp1 = null;
		this.dmq1 = null;
		this.coeff = null
	}
	function r(z, t) {
		if (z != null && t != null && z.length > 0
				&& t.length > 0) {
			this.n = j(z, 16);
			this.e = parseInt(t, 16)
		} else {
			alert("Invalid RSA public key")
		}
	}
	function Y(t) {
		return t.modPowInt(this.e, this.n)
	}
	function s(aD) {
		var t = ah(aD, (this.n.bitLength() + 7) >> 3);
		if (t == null) {
			return null
		}
		var aE = this.doPublic(t);
		if (aE == null) {
			return null
		}
		var z = aE.toString(16);
		if ((z.length & 1) == 0) {
			return z
		} else {
			return "0" + z
		}
	}
	RSAKey.prototype.doPublic = Y;
	RSAKey.prototype.setPublic = r;
	RSAKey.prototype.encrypt = s;
	//暴露RSAKey
	this.RSAKey = RSAKey;
}).call(sinaSSOEncoder);


var encrypt = function(rsaPubkey,password){
	var RSAKey = new sinaSSOEncoder.RSAKey();
	RSAKey.setPublic(rsaPubkey, '10001');
	return RSAKey.encrypt(encodeURIComponent(password));
	
}
