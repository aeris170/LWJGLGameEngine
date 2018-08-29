package particles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.util.vector.Matrix4f;

import entities.Camera;
import renderEngine.Loader;
import toolbox.InsertionSort;

public class ParticleMaster {

	private static Map<ParticleTexture, List<Particle>> particles = new HashMap<>();
	private static ParticleRenderer renderer;

	public static void init(final Loader loader, final Matrix4f projectionMatrix) {
		ParticleMaster.renderer = new ParticleRenderer(loader, projectionMatrix);
	}

	public static void update(final Camera camera) {
		final Iterator<Entry<ParticleTexture, List<Particle>>> mapIterator = ParticleMaster.particles.entrySet().iterator();
		while(mapIterator.hasNext()) {
			final Entry<ParticleTexture, List<Particle>> entry = mapIterator.next();
			final List<Particle> list = entry.getValue();
			final Iterator<Particle> iterator = list.iterator();
			while(iterator.hasNext()) {
				final Particle p = iterator.next();
				final boolean stillAlive = p.update(camera);
				if(!stillAlive) {
					iterator.remove();
					if(list.isEmpty()) {
						mapIterator.remove();
					}
				}
			}
			if(!entry.getKey().usesAdditiveBlending()) {
				InsertionSort.sortHighToLow(list);
			}
		}
	}

	public static void renderParticles(final Camera camera) {
		ParticleMaster.renderer.render(ParticleMaster.particles, camera);
	}

	public static void purge() {
		ParticleMaster.renderer.purge();
	}

	public static void addParticle(final Particle particle) {
		final ParticleTexture pt = particle.getTexture();
		List<Particle> list = ParticleMaster.particles.get(pt);
		if(list == null) {
			list = new ArrayList<>();
			ParticleMaster.particles.put(pt, list);
		}
		list.add(particle);
	}
}
